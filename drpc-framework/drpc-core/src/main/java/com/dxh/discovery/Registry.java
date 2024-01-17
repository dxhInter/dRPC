package com.dxh.discovery;

import com.dxh.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

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
     * 从注册中心拉取服务列表
     * @param serviceName 服务名
     * @return 服务列表
     */
    List<InetSocketAddress> lookup(String serviceName, String group);
}
