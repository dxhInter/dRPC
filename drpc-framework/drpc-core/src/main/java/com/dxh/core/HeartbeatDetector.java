package com.dxh.core;

import com.dxh.DrpcBootstrap;
import com.dxh.NettyBootstrapInitializer;
import com.dxh.enumeration.comperss.CompressorFactory;
import com.dxh.discovery.Registry;
import com.dxh.enumeration.RequestType;
import com.dxh.serialize.SerializerFactory;
import com.dxh.transport.message.DrpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 心跳检测
 */
@Slf4j
public class HeartbeatDetector {
    /**
     * 检测心跳
     */
    public static void detectHeartbeat(String serviceName) {
        //从注册中心获取服务列表
        Registry registry = DrpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
        List<InetSocketAddress> addresses = registry.lookup(serviceName);
        for (InetSocketAddress address : addresses) {
            try {
                //缓存连接
                if (!DrpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    DrpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Thread thread = new Thread(() -> new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 2000),
                "heartbeat-detect-thread");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 设置定时任务
     */
    private static class MyTimerTask extends TimerTask{
        @Override
        public void run() {
            //清空缓存
            DrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.clear();
            //遍历所有的channel，发送心跳
            Map<InetSocketAddress, Channel> cache = DrpcBootstrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress, Channel> entry : cache.entrySet()) {
                int tryTimes = 3;
                while (tryTimes > 0) {
                    Channel channel = entry.getValue();
                    long startTime = System.currentTimeMillis();
                    //创建心跳请求
                    DrpcRequest drpcRequest = DrpcRequest.builder()
                            .requestId(DrpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                            .compressType(CompressorFactory.getCompressor(DrpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                            .serializerType(SerializerFactory.getSerializer(DrpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                            .requestType(RequestType.HEARTBEAT.getId())
                            .timeStamp(startTime)
                            .build();
                    //写出报文
                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    //将completableFuture暴露
                    DrpcBootstrap.PENDING_REQUEST.put(drpcRequest.getRequestId(), completableFuture);
                    channel.writeAndFlush(drpcRequest).addListener((ChannelFutureListener) promise -> {
                        if (!promise.isSuccess()) {
                            completableFuture.completeExceptionally(promise.cause());
                        }
                    });
                    Long endTime = 0L;
                    try {
                        //阻塞方法
                        completableFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
//                    throw new RuntimeException(e);
                        tryTimes--;
                        log.error("和地址为:{{}}主机连接异常", channel.remoteAddress());
                        //移除异常的channel
                        if (tryTimes == 0) {
                            DrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                        }
                        continue;
                    }
                    try {
                        Thread.sleep(10*(new Random().nextInt(5)));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Long time = endTime - startTime;
                    //使用treeMap进行缓存，并排序
                    DrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time, channel);
                    log.debug("服务器返回时间[{}] 是 :[{}]", entry.getKey(), time);
                    break;
                }
            }
            log.info("-----------------生成treemap-----------------");
            for (Map.Entry<Long, Channel> entry : DrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.entrySet()) {
                if (log.isDebugEnabled()){
                    log.debug("服务器[{}]返回时间是 :[{}]",entry.getValue().id(), entry.getKey());
                }
            }
        }
    }

}
