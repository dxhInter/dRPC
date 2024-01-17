package com.dxh.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡器接口
 */
public interface LoadBalancer {
    /**
     * 根据服务名获取一个可用服务
     * @param serviceName 服务名
     * @return 服务地址
     */
    InetSocketAddress selectServiceAddress(String serviceName, String group);

    /**
     * ，当感知节点发生动态上下线, 重新加载负载均衡器
     * @param serviceName 服务名
     * @param addresses 服务地址列表
     */
    void reLoadBalance(String serviceName, List<InetSocketAddress> addresses);
}
