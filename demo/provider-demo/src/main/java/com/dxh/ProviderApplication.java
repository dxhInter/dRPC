package com.dxh;

import com.dxh.impl.HelloDPRCImpl;

public class ProviderApplication {
    public static void main(String[] args) {
        // 服务提供方，需要注册服务，启动服务
        // 1、封装要发布的服务
        ServiceConfig<HelloDRPC> service = new ServiceConfig<>();
        service.setInterface(HelloDRPC.class);
        service.setRef(new HelloDPRCImpl());

        //服务提供方，需要注册
        //1. 封装服务提供方的信息
        //2. 向注册中心注册服务
        //3. 启动服务
        DrpcBootstrap.getInstance()
                .application("first-drpc-provider")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig("jdk"))
//                .publish(service)
                .scan("com.dxh")
                .start();
    }

}