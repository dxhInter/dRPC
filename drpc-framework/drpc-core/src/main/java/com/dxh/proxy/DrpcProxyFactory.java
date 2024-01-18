package com.dxh.proxy;

import com.dxh.DrpcBootstrap;
import com.dxh.ReferenceConfig;
import com.dxh.RegistryConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代理工厂
 */
public class DrpcProxyFactory {

    private static Map<Class<?>,Object> cache = new ConcurrentHashMap<>(32);

    public static <T> T getProxy(Class<T> clazz) {
        Object bean = cache.get(clazz);
        if(bean!=null){
            return (T) bean;
        }

        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterface(clazz);

        // 1. 连接注册中心
        // 2. 获取服务提供方的信息
        // 3. 通过网络请求，调用服务提供方的方法
        // 4. 发送请求，获取响应(接口名，方法名，参数)
        DrpcBootstrap.getInstance()
                .application("first-drpc-comsumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("hessian")
                .compress("gzip")
                .group("primary")
                .reference(reference);

        System.out.println("=================================================================");
        // 获取一个代理对象
        T t = reference.get();
        cache.put(clazz,t);
        return t;
    }
}
