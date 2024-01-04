package com.dxh;

import com.dxh.discovery.Registry;
import com.dxh.discovery.impl.NacosRegistry;
import com.dxh.discovery.impl.ZookeeperRegistry;
import com.dxh.exceptions.DiscoveryException;

public class ResgistryConfig {
    private String connectString;

    public ResgistryConfig(String connectString) {
        this.connectString = connectString;
    }

    /**
     * 获取注册中心实例，使用简单工厂模式
     * @return
     */
    public Registry getRegistry() {
        String registryType = getRegistryType(connectString, true).toLowerCase().trim();
        if (registryType.equals("zookeeper")) {
            String host = getRegistryType(connectString, false);
            return new ZookeeperRegistry(host,Constant.ZK_SESSION_TIMEOUT);
        } else if(registryType.equals("nacos")){
            String host = getRegistryType(connectString, false);
            return new NacosRegistry(host,Constant.ZK_SESSION_TIMEOUT);
        }
        throw new DiscoveryException("registry type is not supported");
    }

    private String getRegistryType(String connectString, boolean isType){
        String[] typeAndHost = connectString.split("://");
        if(typeAndHost.length != 2){
            throw new RuntimeException("registry config center error, the given url is illegal and the format must be zookeeper://host:port");
        }
        if(isType){
            return typeAndHost[0];
        }else {
            return typeAndHost[1];
        }
    }
}
