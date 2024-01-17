package com.dxh.channelhandler.handler;

import com.dxh.DrpcBootstrap;
import com.dxh.enumeration.ResponseCode;
import com.dxh.exceptions.ResponseException;
import com.dxh.loadbalancer.LoadBalancer;
import com.dxh.protection.Breaker;
import com.dxh.transport.message.DrpcRequest;
import com.dxh.transport.message.DrpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 测试
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<DrpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DrpcResponse drpcResponse) throws Exception {
        CompletableFuture<Object> completableFuture = DrpcBootstrap.PENDING_REQUEST.get(drpcResponse.getRequestId());

        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        Map<SocketAddress, Breaker> everyIpBreaker = DrpcBootstrap.getInstance().getConfiguration().getEveryIpBreaker();
        Breaker breaker = everyIpBreaker.get(socketAddress);

        byte code = drpcResponse.getCode();
        if(code == ResponseCode.FAIL.getCode()){
            breaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("the request is fail, the requestId is [{}], the code is [{}]",drpcResponse.getRequestId(),drpcResponse.getCode());
            throw new ResponseException(code,ResponseCode.FAIL.getDesc());
        } else if(code == ResponseCode.RATE_LIMITING.getCode()){
            breaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("the request is RATE_LIMITING, the requestId is [{}], the code is [{}]",drpcResponse.getRequestId(),drpcResponse.getCode());
            throw new ResponseException(code,ResponseCode.RATE_LIMITING.getDesc());
        } else if (code == ResponseCode.RESOURCE_NOT_FOUND.getCode()){
            breaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("the request is RESOURCE_NOT_FOUND, the requestId is [{}], the code is [{}]",drpcResponse.getRequestId(),drpcResponse.getCode());
            throw new ResponseException(code,ResponseCode.RESOURCE_NOT_FOUND.getDesc());
        } else if (code == ResponseCode.SUCCESS.getCode()){
            // 1. 获取负载内容
            Object returnValue = drpcResponse.getBody();
            completableFuture.complete(returnValue);
            if (log.isDebugEnabled()){
                log.debug("already find the id[{}]'s completableFuture",drpcResponse.getRequestId());
            }
        } else if (code == ResponseCode.SUCCESS_HEARTBEAT.getCode()) {
            completableFuture.complete(null);
            if (log.isDebugEnabled()){
                log.debug("already find the id[{}]'s completableFuture, 处理心跳检测",drpcResponse.getRequestId());
            }
        } else if (code == ResponseCode.CLOSING.getCode()) {
            completableFuture.complete(null);
            if (log.isDebugEnabled()){
                log.debug("the request is CLOSING, the requestId is [{}], the code is [{}]"
                        ,drpcResponse.getRequestId(),drpcResponse.getCode());
            }
            //修正负载均衡器, 从健康的节点中移除
            DrpcBootstrap.CHANNEL_CACHE.remove(socketAddress);
            //找到负载均衡器
            LoadBalancer loadBalancer = DrpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
            DrpcRequest drpcRequest = DrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            //重新负载均衡
            loadBalancer.reLoadBalance(drpcRequest.getPayload().getInterfaceName()
                    ,DrpcBootstrap.CHANNEL_CACHE.keySet().stream().toList());
            throw new ResponseException(code,ResponseCode.CLOSING.getDesc());
        }
    }
}
