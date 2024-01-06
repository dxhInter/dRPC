package com.dxh;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        // 获取代理对象
        // 1. 封装服务提供方的信息
        ReferenceConfig<HelloDRPC> reference = new ReferenceConfig<>();
        reference.setInterface(HelloDRPC.class);

        // 1. 连接注册中心
        // 2. 获取服务提供方的信息
        // 3. 通过网络请求，调用服务提供方的方法
        // 4. 发送请求，获取响应(接口名，方法名，参数)
        DrpcBootstrap.getInstance()
                .application("first-drpc-comsumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(reference);

        // 获取一个代理对象
        HelloDRPC helloDRPC = reference.get();
        String sayHi = helloDRPC.sayHello("你好");
        log.info("sayHi is :{}", sayHi);
    }
}
