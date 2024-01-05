package com.dxh;

import com.dxh.discovery.Registry;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 *
 * @param <T>
 */
@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    private Registry registry;

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    /**
     * 代理设计模式，生成一个api接口的代理对象
     * @return
     */
    public T get() {
        //动态代理
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};

        //使用动态代理生产代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                System.out.println("hello proxy");
                log.info("method is :{}", method.getName());
                log.info("args is :{}", args);

                //1. 发现服务，从注册中心找到一个可用的服务
                //传入接口名
                InetSocketAddress address = registry.lookup(interfaceRef.getName());
                if (log.isInfoEnabled()) {
                    log.debug("address is :{}, and consumer get the interface of {}",
                            address, interfaceRef.getName());
                }


                //2. 通过netty连接服务器，发送调用的服务名字、方法名、参数列表，调用服务提供方的方法
                NioEventLoopGroup group = new NioEventLoopGroup();

                try {
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(group)
                            .channel(NioSocketChannel.class)
                            .remoteAddress(address)
                            .handler(new ChannelInitializer<Channel>()
                            {

                                @Override
                                protected void initChannel(Channel channel) throws Exception {
                                    channel.pipeline().addLast(null);
                                }
                            });
                    ChannelFuture future = bootstrap.connect().sync();
                    future.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty server", CharsetUtil.UTF_8));
                    //阻塞程序，等到接收消息
                    future.channel().closeFuture().sync();

                }catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    group.shutdownGracefully();
                }
                return null;
            }
        });
        return (T) helloProxy;
    }
}
