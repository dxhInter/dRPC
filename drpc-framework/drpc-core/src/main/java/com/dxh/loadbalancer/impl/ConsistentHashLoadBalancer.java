package com.dxh.loadbalancer.impl;

import com.dxh.DrpcBootstrap;
import com.dxh.exceptions.LoadBalancerException;
import com.dxh.loadbalancer.AbstractLoadBalancer;
import com.dxh.loadbalancer.Selector;
import com.dxh.transport.message.DrpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡策略
 */
@Slf4j
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashLoadSelector(serviceList,128);
    }

    /**
     * 一致性hash负载均衡策略的具体实现
     */
    private static class ConsistentHashLoadSelector implements Selector{
        //hash环存储的是服务的地址
        private SortedMap<Integer,InetSocketAddress> circle= new TreeMap<>();
        //虚拟节点的个数
        private int virtualNodes;

        public ConsistentHashLoadSelector(List<InetSocketAddress> serviceList, int virtualNodes) {
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress inetSocketAddress : serviceList) {
                //将每个节点加入环
                addNodeToCircle(inetSocketAddress);
            }
        }

        /**
         * 将节点加入环
         * @param inetSocketAddress
         */
        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            //根据虚拟节点的个数，为每个节点创建虚拟节点
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" +i);
                //将虚拟节点加入环中
                circle.put(hash,inetSocketAddress);
                if (log.isDebugEnabled()){
                    log.debug("add node to circle, node is :{}, hash is :{}",inetSocketAddress,hash);
                }
            }
        }

        /**
         * 将节点从环中移除
         * @param inetSocketAddress
         */
        private void removeNodeFromCircle(InetSocketAddress inetSocketAddress) {
            //根据虚拟节点的个数，为每个节点创建虚拟节点
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" +i);
                //将虚拟节点加入环中
                circle.remove(hash);
            }
        }

        /**
         * 具体hash算法，使用md5
         * @param s
         * @return
         */
        private int hash(String s) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");

            } catch (NoSuchAlgorithmException e) {
                throw new LoadBalancerException("hash error");
            }
            byte[] digest = md.digest(s.getBytes());
            //digest是 字节数组，需要转换成int 4个字节
            int res = 0;
            for (int i = 0; i < 4; i++) {
                int mid = digest[i] << ((3 - i) * 8);
                res = res | mid;
            }
            return res;
        }

        @Override
        public InetSocketAddress getNext() {
            DrpcRequest drpcRequest = DrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            String requestId = Long.toString(drpcRequest.getRequestId());
            int hash = hash(requestId);
            //判断该hash值是否在环中
            if (!circle.containsKey(hash)){
                //寻找大于该hash值的第一个节点，也就是顺时针方向最近的节点，tailMap返回的是大于等于该hash值的节点
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                //如果tailMap为空，那么就返回circle中的第一个节点
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }

            return circle.get(hash);
        }

        @Override
        public void rebalance() {

        }
    }
}
