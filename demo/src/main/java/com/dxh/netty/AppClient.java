package com.dxh.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class AppClient {

    private final int port;

    private final String HOST;

    public AppClient(int port, String host){
        this.port = port;
        HOST = host;
    }
    public void run() throws Exception{
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(port))
                    .handler(new ChannelInitializer<Channel>()
                    {

                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(new HandlerClient());
                        }
                    });
            ChannelFuture future = bootstrap.connect().sync();
            future.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty server", CharsetUtil.UTF_8));
            future.channel().closeFuture().sync();

        }finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        AppClient client = new AppClient(8082, "127.0.0.1");
        try {
            client.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
