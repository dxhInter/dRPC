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
    InetSocketAddress selectServiceAddress(String serviceName);
}
