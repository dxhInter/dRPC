package com.dxh.loadbalancer;

import com.dxh.DrpcBootstrap;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLoadBalancer implements LoadBalancer{
    //一个服务对应一个selector
    private Map<String,Selector> cache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName, String group) {
        //如果缓存中有，那么就直接从缓存中获取选择器
        Selector selector = cache.get(serviceName);

        if (selector == null) {
            //如果没有，那么就创建一个新的
            //该负载均衡器，内部维护服务列表作为缓存
            List<InetSocketAddress> serviceList = DrpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry().lookup(serviceName,group);
            //选取一个可用的服务
            selector = getSelector(serviceList);
            //放入缓存
            cache.put(serviceName,selector);
        }

        return selector.getNext();
    }

    @Override
    public synchronized void reLoadBalance(String serviceName, List<InetSocketAddress> addresses) {
        //todo 需要继续完成
        cache.put(serviceName,getSelector(addresses));
    }

    /**
     * 由子类进行扩展
     * @param serviceList
     * @return
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);
}
