package com.dxh;

import com.dxh.channelHandler.handler.DrpcRequestDecoder;
import com.dxh.channelHandler.handler.DrpcResponseEncoder;
import com.dxh.channelHandler.handler.MethodCallHandler;
import com.dxh.discovery.Registry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DrpcBootstrap {
    // DrpcBootstrap 是一个单例，饿汉式
    private static final DrpcBootstrap drpcBootstrap = new DrpcBootstrap();

    //定义相关基础配置
    private String applicationName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private Registry registry;
    //定义服务列表
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);

    //定义服务端的channel缓存
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

    //定义全局的对外挂起的completableFuture
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    private int port = 8082;
    public final static IdGenerator ID_GENERATOR = new IdGenerator(1,2);
    public static String SERIALIZE_TYPE = "jdk";

    private DrpcBootstrap() {
    }

    public static DrpcBootstrap getInstance() {
        return drpcBootstrap;
    }

    /**
     * 定义名字
     * @param applicationName
     * @return
     */
    public DrpcBootstrap application(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    /**
     * 配置注册中心
     * @param registryConfig
     * @return
     */
    public DrpcBootstrap registry(RegistryConfig registryConfig) {
        //创建zookeeper连接实例，ps耦合

        this.registry = registryConfig.getRegistry();
        return this;

    }

    /**
     * 初始序列化,配置当前暴露的服务所使用的协议
     * @param protocolConfig
     * @return
     */
    public DrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if (log.isInfoEnabled()) {
            log.debug("序列化协议:{}，已经被注册", protocolConfig.toString());
        }
        return this;
    }
    /**
     * 发布服务,将接口的实现类发布到注册中心
     * @param service 封装的需要服务
     * @return this
     */
    public DrpcBootstrap publish(ServiceConfig<?> service) {
        //抽象注册中心的概念，使用注册中心的实现完成实现
        registry.register(service);

        //当服务调用方通过接口、方法名、参数列表发起调用时，服务提供方需要根据这些信息找到对应的服务
        SERVICE_LIST.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 批量发布服务
     * @param services 封装的需要服务的集合
     * @return this
     */
    public DrpcBootstrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services) {
            publish(service);
        }
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(10);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap = serverBootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler())
                                    .addLast(new DrpcRequestDecoder())
                                    .addLast(new MethodCallHandler())
                                    .addLast(new DrpcResponseEncoder());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(port).sync();
//            System.out.println("server started and listen, and hello" + future.channel().localAddress());
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * -----------------------------------------------服务消费方api-------------------------------------------------------
     */

    /**
     * 引用服务
     * @param reference
     * @return this
     */
    public DrpcBootstrap reference(ReferenceConfig<?> reference) {
        //配置reference，将来调用get方法，方便生成代理对象
        reference.setRegistry(registry);
        return this;
    }

    /**
     * 设置序列化方式
     * @param serializeType
     * @return
     */
    public DrpcBootstrap serialize(String serializeType) {
        SERIALIZE_TYPE = serializeType;
        if (log.isInfoEnabled()) {
            log.debug("serializer's type:{}，has been registered", serializeType);
        }
        return this;
    }
}

/**
 * 1、服务调用方
 * 发送报文 writeAndFlush(object) 请求
 * 此object应该是什么?应该包含一些什么样的信息?
 * YrpcRequest
 * 1、请求id
 * 2、压缩类型 (1byte)
 * 3、序列化的方式(1byte)
 * 4、消息类型(普通请求,心跳检测请求)
 * 5、负载 payload(接口的名字,方法的名字,参数列表,返回值类型))
 * pipeline就生效了,报文开始出站
 * - 第一个处理器(out)(转化 object->msg(请求报文))
 * - 第二个处理器(out)(序列化)
 * - 第三个处理器(out)(压缩)
 */
