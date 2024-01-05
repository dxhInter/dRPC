package com.dxh.discovery;

import com.dxh.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * 服务注册中心接口
 */
public interface Registry {
    /**
     * 注册服务
     * @param serviceConfig
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉去一个可用的服务
     * @param serviceName 服务名
     * @return 服务地址+端口
     */
    InetSocketAddress lookup(String serviceName);
}
