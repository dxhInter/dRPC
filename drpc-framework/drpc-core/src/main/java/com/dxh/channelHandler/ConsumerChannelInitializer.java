package com.dxh.channelHandler;

import com.dxh.channelHandler.handler.DrpcRequestEncoder;
import com.dxh.channelHandler.handler.DrpcResponseDecoder;
import com.dxh.channelHandler.handler.MySimpleChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new LoggingHandler(LogLevel.DEBUG)) //netty自带日志打印
                .addLast(new DrpcRequestEncoder()) //消息编码器，出站
                .addLast(new DrpcResponseDecoder()) //消息解码器，入站
                .addLast(new MySimpleChannelInboundHandler());
    }
}
