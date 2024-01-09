package com.dxh.proxy.handler;

import com.dxh.DrpcBootstrap;
import com.dxh.IdGenerator;
import com.dxh.NettyBootstrapInitializer;
import com.dxh.comperss.CompressorFactory;
import com.dxh.discovery.Registry;
import com.dxh.enumeration.RequestType;
import com.dxh.exceptions.DiscoveryException;
import com.dxh.exceptions.NetworkException;
import com.dxh.serialize.SerializerFactory;
import com.dxh.transport.message.DrpcRequest;
import com.dxh.transport.message.RequestPayload;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 此类封装了客户端通信的逻辑，代理对象的远程调用封装到了invoke方法中
 * 1，发现可用服务 2，建立连接 3，发送请求 4，接收响应
 */
@Slf4j
public class RPCConsumerInvocationHandler implements InvocationHandler {

    //注册中心
    private Registry registry;
    private Class<?> interfaceRef;

    public RPCConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //封装报文
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .parameterValue(args)
                .returnType(method.getReturnType())
                .build();
        DrpcRequest drpcRequest = DrpcRequest.builder()
                .requestId(DrpcBootstrap.ID_GENERATOR.getId())
                .compressType(CompressorFactory.getCompressor(DrpcBootstrap.COMPRESS_TYPE).getCode())
                .serializerType(SerializerFactory.getSerializer(DrpcBootstrap.SERIALIZE_TYPE).getCode())
                .requestType(RequestType.REQUEST.getId())
                .payload(requestPayload)
                .build();

        //将request放入到threadLocal中,需要在合适的时机清除remove
        DrpcBootstrap.REQUEST_THREAD_LOCAL.set(drpcRequest);


        //获取当前配置的负载均衡器，选取可用的服务
        //传入服务的名字，获取可用的服务地址ip+port
        InetSocketAddress address = DrpcBootstrap.LOAD_BALANCER.selectServiceAddress(interfaceRef.getName());
        if (log.isInfoEnabled()) {
            log.debug("address is :{}, and consumer get the interface of {}",
                    address, interfaceRef.getName());
        }


        //2. 通过netty连接服务器，发送调用的服务名字、方法名、参数列表，调用服务提供方的方法
        Channel channel = getAvailableChannel(address);
        if (log.isInfoEnabled()) {
            log.debug("get the channel [{}] with the address [{}]", channel,address);
        }



//                异步策略
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        DrpcBootstrap.PENDING_REQUEST.put(1L, completableFuture);

        //发送drpcrequest请求, 实例进入到pipeline中, 转换为二进制报文
        channel.writeAndFlush(drpcRequest).addListener((ChannelFutureListener) promise -> {
            //当前的promise返回的结果是writeAndFlush的结果
//                    if (promise.isDone()){
//                        completableFuture.complete(promise.getNow());
//                    }
            if (!promise.isSuccess()){
                completableFuture.completeExceptionally(promise.cause());
            }
        });
        //清理threadLocal
        DrpcBootstrap.REQUEST_THREAD_LOCAL.remove();
        return completableFuture.get(10, TimeUnit.SECONDS);
    }

    /**
     * 从地址中获取可用的channel
     * @param address
     * @return
     */
    private Channel getAvailableChannel(InetSocketAddress address) {
        Channel channel = DrpcBootstrap.CHANNEL_CACHE.get(address);
        //使用异步的方式
        if (channel == null) {
            //连接服务器,await()阻塞，直到连接成功，提供异步处理逻辑
//                    channel = NettyBootstrapInitializer.getBootstrap()
//                            .connect(address).await().channel();
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(address).addListener(
                    (ChannelFutureListener) promise -> {
                        if (promise.isDone()){
                            if (log.isInfoEnabled()) {
                                log.debug("already created connection to [{}] success", address);
                            }
                            channelFuture.complete(promise.channel());
                        } else if (!promise.isSuccess()) {
                            channelFuture.completeExceptionally(promise.cause());
                        }
                    }
            );
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("get channel error: ", e);
                throw new DiscoveryException(e);
            }
            //将channel缓存起来
            DrpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }

        if (channel == null) {
            throw new NetworkException("channel is null, an exception occurred when getting channel");
        }
        return channel;
    }
}
