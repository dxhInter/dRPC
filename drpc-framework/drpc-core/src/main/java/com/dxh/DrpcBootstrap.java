package com.dxh;

import com.dxh.annotation.DrpcApi;
import com.dxh.channelhandler.handler.DrpcRequestDecoder;
import com.dxh.channelhandler.handler.DrpcResponseEncoder;
import com.dxh.channelhandler.handler.MethodCallHandler;
import com.dxh.core.HeartbeatDetector;
import com.dxh.discovery.Registry;
import com.dxh.loadbalancer.LoadBalancer;
import com.dxh.transport.message.DrpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class DrpcBootstrap {

    // DrpcBootstrap 是一个单例，饿汉式
    private static final DrpcBootstrap drpcBootstrap = new DrpcBootstrap();
    //全局配置中心
    private Configuration configuration;
    //保存request对象，在当前线程中，可以通过这个对象获取到request对象
    public static final ThreadLocal<DrpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();


    //定义服务列表
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new ConcurrentHashMap<>(16);

    //定义服务端的channel缓存
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    public static final TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();

    //定义全局的对外挂起的completableFuture
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);



    private DrpcBootstrap() {
        //构造上下文
        configuration = new Configuration();
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
        configuration.setApplicationName(applicationName);
        return this;
    }

    /**
     * 配置注册中心
     * @param registryConfig
     * @return
     */
    public DrpcBootstrap registry(RegistryConfig registryConfig) {
        //创建zookeeper连接实例, 使用registryConfig获取注册中心
        configuration.setRegistryConfig(registryConfig);
        return this;

    }

    /**
     * 配置负载均衡策略
     * @param loadBalancer
     * @return
     */
    public DrpcBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }

    /**
     * 初始序列化,配置当前暴露的服务所使用的协议
     * @param protocolConfig
     * @return
     */
    public DrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        configuration.setProtocolConfig(protocolConfig);
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
        configuration.getRegistryConfig().getRegistry().register(service);
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
            ChannelFuture future = serverBootstrap.bind(configuration.getPort()).sync();

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
        //开启对这个服务的心跳检测
        HeartbeatDetector.detectHeartbeat(reference.getInterface().getName());
        //配置reference，将来调用get方法，方便生成代理对象
        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        return this;
    }

    /**
     * 设置序列化方式
     * @param serializeType
     * @return
     */
    public DrpcBootstrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        if (log.isInfoEnabled()) {
            log.debug("serializer's type:{}，has been registered", serializeType);
        }
        return this;
    }

    /**
     * 设置压缩方式
     * @param compressType
     * @return
     */
    public DrpcBootstrap compress(String compressType) {
        configuration.setCompressType(compressType);
        if (log.isInfoEnabled()) {
            log.debug("compressor's type:{}，has been registered", compressType);
        }
        return this;
    }

    /**
     * 通过包扫描，拿到类的名字，暴露接口，将服务发布
     * @param packageName
     * @return
     */
    public DrpcBootstrap scan(String packageName) {
        // 获取到所有的类的全限定名
        List<String> classNames = getAllClassNames(packageName);
        //通过反射获取到所有的接口, 暴露服务
        List<Class<?>> classes = classNames.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(clazz -> clazz.getAnnotation(DrpcApi.class) != null).collect(Collectors.toList());
        for (Class<?> clazz :classes){
            //获取接口
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                //无参数的构造器
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            List<ServiceConfig<?>> serviceConfigs = new ArrayList<>();
            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                if (log.isDebugEnabled()) {
                    log.debug("通过包扫描将服务发布:[{}}", anInterface);
                }
                publish(serviceConfig);
            }
        }
        return this;
    }

    private List<String> getAllClassNames(String packageName) {
        //通过packageName获取到绝对路径,将.替换成/
        String packagePath = packageName.replaceAll("\\.", "/");
//        String packagePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(packagePath);
        if (url == null){
            throw new RuntimeException("path not found when scan package");
        }
        String absolutePath = url.getPath();
        List<String> classNames = new ArrayList<>();
        classNames = recursionFile(absolutePath,classNames,packagePath);
        return classNames;
    }

    private List<String> recursionFile(String absolutePath, List<String> classNames,String packagePath) {
        //获取到当前路径下的所有文件
        File file = new File(absolutePath);
        //判断文件是否是文件夹
        if (file.isDirectory()) {
            //如果是文件夹，找到所有的子文件
            File[] childrenFiles = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (childrenFiles == null || childrenFiles.length == 0) {
                return classNames;
            }
            for (File child : childrenFiles) {
                if (child.isDirectory()){
                    //如果是文件夹，递归
                    recursionFile(child.getAbsolutePath(),classNames,packagePath);
                }else {
                    //文件名 --》 类名
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath(),packagePath);
                    classNames.add(className);
                }
            }
        }else {
            //如果是文件，
            String className = getClassNameByAbsolutePath(absolutePath,packagePath);
            classNames.add(className);
        }
        return classNames;
    }

    private String getClassNameByAbsolutePath(String absolutePath,String packagePath) {
        String fileName = absolutePath.substring(absolutePath.indexOf(packagePath.replaceAll("/","\\\\"))).replaceAll("\\\\",".");
        fileName = fileName.substring(0,fileName.indexOf(".class"));
        return fileName;
    }

    public static void main(String[] args) {
        List<String> classNames = DrpcBootstrap.getInstance().getAllClassNames("com.dxh");
        System.out.println(classNames);
    }

    public Configuration getConfiguration() {
        return configuration;
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
