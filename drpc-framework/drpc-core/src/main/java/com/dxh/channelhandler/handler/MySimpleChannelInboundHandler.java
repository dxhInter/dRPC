package com.dxh.channelhandler.handler;

import com.dxh.DrpcBootstrap;
import com.dxh.transport.message.DrpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 测试
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<DrpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DrpcResponse drpcResponse) throws Exception {
        // 1. 获取负载内容
        Object returnValue = drpcResponse.getBody();
        CompletableFuture<Object> completableFuture = DrpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(returnValue);
        if (log.isDebugEnabled()){
            log.debug("already find the id[{}]'s completableFuture",drpcResponse.getRequestId());
        }
    }
}
