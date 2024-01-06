package com.dxh;


import com.dxh.channelHandler.ConsumerChannelInitializer;
import com.dxh.channelHandler.handler.MySimpleChannelInboundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 提供nettybootstrap的单例
 */
@Slf4j
public class NettyBootstrapInitializer {
    private static final Bootstrap bootstrap = new Bootstrap();

    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ConsumerChannelInitializer());
    }

    private NettyBootstrapInitializer() {}

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
