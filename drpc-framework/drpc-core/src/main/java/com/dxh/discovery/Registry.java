package com.dxh.discovery;

import com.dxh.ServiceConfig;

/**
 * 服务注册中心接口
 */
public interface Registry {
    /**
     * 注册服务
     * @param serviceConfig
     */
    void register(ServiceConfig<?> serviceConfig);
}
