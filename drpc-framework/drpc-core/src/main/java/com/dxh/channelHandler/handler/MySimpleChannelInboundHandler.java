package com.dxh.channelHandler.handler;

import com.dxh.DrpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * 测试
 */
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        String result = byteBuf.toString(Charset.defaultCharset());
        CompletableFuture<Object> completableFuture = DrpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}
