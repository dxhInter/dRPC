package com.dxh.loadbalancer.impl;

import com.dxh.DrpcBootstrap;
import com.dxh.discovery.Registry;
import com.dxh.exceptions.LoadBalancerException;
import com.dxh.loadbalancer.AbstractLoadBalancer;
import com.dxh.loadbalancer.LoadBalancer;
import com.dxh.loadbalancer.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡策略
 */
@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinLoadSelector(serviceList);
    }

    private static class RoundRobinLoadSelector implements Selector{
        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;


        public RoundRobinLoadSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if (serviceList == null || serviceList.size() == 0) {
                log.error("using RoundRobin to load balance, but serviceList is empty");
                throw new LoadBalancerException("using RoundRobin to load balance, serviceList is empty");
            }
            InetSocketAddress address = serviceList.get(index.get());
            if (index.get() == serviceList.size()-1) {
                //如果已经到了最后一个，那么就从头开始
                index.set(0);
            }else {
                //否则就递增
                index.incrementAndGet();
            }
            return address;
        }

        @Override
        public void rebalance() {

        }
    }
}
