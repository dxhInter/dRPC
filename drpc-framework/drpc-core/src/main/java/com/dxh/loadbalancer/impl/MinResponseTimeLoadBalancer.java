package com.dxh.loadbalancer.impl;

import com.dxh.DrpcBootstrap;
import com.dxh.exceptions.LoadBalancerException;
import com.dxh.loadbalancer.AbstractLoadBalancer;
import com.dxh.loadbalancer.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
@Slf4j
public class MinResponseTimeLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinResponseTimeSelector(serviceList);
    }
    private static class MinResponseTimeSelector implements Selector{

        public MinResponseTimeSelector(List<InetSocketAddress> serviceList) {

        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = DrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if (entry != null){
                if (log.isDebugEnabled()){
                    log.debug("选择了响应时间为:[{}]的节点",entry.getKey());
                }
                return  (InetSocketAddress) entry.getValue().remoteAddress();
            }
            //如果没有，就随机获取一个
            Channel channel = (Channel)DrpcBootstrap.CHANNEL_CACHE.values().toArray()[0];

            return (InetSocketAddress) channel.remoteAddress();
        }
    }
}
