package com.dxh.proxy.handler;

import com.dxh.DrpcBootstrap;
import com.dxh.NettyBootstrapInitializer;
import com.dxh.annotation.TryTimes;
import com.dxh.comperss.CompressorFactory;
import com.dxh.discovery.Registry;
import com.dxh.enumeration.RequestType;
import com.dxh.exceptions.DiscoveryException;
import com.dxh.exceptions.NetworkException;
import com.dxh.protection.Breaker;
import com.dxh.serialize.SerializerFactory;
import com.dxh.transport.message.DrpcRequest;
import com.dxh.transport.message.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.CircuitBreaker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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

    /**
     * 代理对象的远程调用封装到了invoke方法中
     * @param proxy the proxy instance that the method was invoked on
     *
     * @param method the {@code Method} instance corresponding to
     * the interface method invoked on the proxy instance.  The declaring
     * class of the {@code Method} object will be the interface that
     * the method was declared in, which may be a superinterface of the
     * proxy interface that the proxy class inherits the method through.
     *
     * @param args an array of objects containing the values of the
     * arguments passed in the method invocation on the proxy instance,
     * or {@code null} if interface method takes no arguments.
     * Arguments of primitive types are wrapped in instances of the
     * appropriate primitive wrapper class, such as
     * {@code java.lang.Integer} or {@code java.lang.Boolean}.
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //从接口中获取是否需要重试
        TryTimes tryTimesAnnotation = method.getAnnotation(TryTimes.class);

        int tryTime = 0;
        int intervalTime = 0;
        if(tryTimesAnnotation != null){
            tryTime = tryTimesAnnotation.tryTimes();
            intervalTime = tryTimesAnnotation.intervalTime();
        }

        while(true) {
            //封装报文
            RequestPayload requestPayload = RequestPayload.builder()
                    .interfaceName(interfaceRef.getName())
                    .methodName(method.getName())
                    .parameterTypes(method.getParameterTypes())
                    .parameterValue(args)
                    .returnType(method.getReturnType())
                    .build();
            DrpcRequest drpcRequest = DrpcRequest.builder()
                    .requestId(DrpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                    .compressType(CompressorFactory.getCompressor(DrpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                    .serializerType(SerializerFactory.getSerializer(DrpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                    .requestType(RequestType.REQUEST.getId())
                    .timeStamp(System.currentTimeMillis())
                    .payload(requestPayload)
                    .build();

            //将request放入到threadLocal中,需要在合适的时机清除remove
            DrpcBootstrap.REQUEST_THREAD_LOCAL.set(drpcRequest);


            //获取当前配置的负载均衡器，选取可用的服务
            //传入服务的名字，获取可用的服务地址ip+port
            InetSocketAddress address = DrpcBootstrap.getInstance().getConfiguration().getLoadBalancer().selectServiceAddress(interfaceRef.getName());
            if (log.isInfoEnabled()) {
                log.debug("address is :{}, and consumer get the interface of {}",
                        address, interfaceRef.getName());
            }
            //获取当前ip的断路器
            Map<SocketAddress, Breaker> everyIpBreaker = DrpcBootstrap.getInstance().getConfiguration().getEveryIpBreaker();
            Breaker breaker = everyIpBreaker.get(address);
            if (breaker == null) {
                //如果没有断路器，创建一个断路器
                breaker = new Breaker(10,0.5f);
                everyIpBreaker.put(address, breaker);
            }
            try {
                //如果断路器是打开的,并且不是心跳请求
                if (drpcRequest.getRequestType() != RequestType.HEARTBEAT.getId() && breaker.isBreak()) {
                    //定期打开断路器
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            //5秒后重置断路器
                            DrpcBootstrap.getInstance()
                                    .getConfiguration().getEveryIpBreaker()
                                    .get(address).reset();
                        }
                    }, 5000);
                    throw new RuntimeException("当前断路器已经打开，无法发送请求");
                }

                //2. 通过netty连接服务器，发送调用的服务名字、方法名、参数列表，调用服务提供方的方法
                Channel channel = getAvailableChannel(address);
                if (log.isInfoEnabled()) {
                    log.debug("get the channel [{}] with the address [{}]", channel, address);
                }


//                异步策略
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                DrpcBootstrap.PENDING_REQUEST.put(drpcRequest.getRequestId(), completableFuture);

                //发送drpcrequest请求, 实例进入到pipeline中, 转换为二进制报文
                channel.writeAndFlush(drpcRequest).addListener((ChannelFutureListener) promise -> {
                    //当前的promise返回的结果是writeAndFlush的结果
//                    if (promise.isDone()){
//                        completableFuture.complete(promise.getNow());
//                    }
                    if (!promise.isSuccess()) {
                        completableFuture.completeExceptionally(promise.cause());
                    }
                });
                //清理threadLocal
                DrpcBootstrap.REQUEST_THREAD_LOCAL.remove();
                Object result = completableFuture.get(10, TimeUnit.SECONDS);
                breaker.recordRequest();
                return result;
            } catch (Exception e) {
                //等待固定时间
                tryTime--;
                breaker.recordErrorRequest();
                try {
                    Thread.sleep(intervalTime);
                } catch (InterruptedException ex) {
                    log.error("thread sleep error when try to reconnect", ex);
                }
                if (tryTime < 0) {
                    log.error("remote invoke method:[{}] error, and exception info is [{}]", method.getName(), e);
                    break;
                }
                log.error("consumer invoke error: ", e);
            }
        }
        throw new RuntimeException("remote invoke method:[" + method.getName() + "] error");
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
