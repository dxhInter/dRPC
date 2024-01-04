package com.dxh;

import com.dxh.discovery.Registry;
import com.dxh.discovery.impl.ZookeeperRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DrpcBootstrap {
    // DrpcBootstrap 是一个单例，饿汉式
    private static final DrpcBootstrap drpcBootstrap = new DrpcBootstrap();

    //定义相关基础配置
    private String applicationName = "default";
    private ResgistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private Registry registry;

    private int port = 8082;

    private DrpcBootstrap() {

    }

    public static DrpcBootstrap getInstance() {
        
        return drpcBootstrap;
    }

    /**
     * 定义名字
     * @param applicationName
     * @return
     */
    public DrpcBootstrap application(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    /**
     * 配置注册中心
     * @param registryConfig
     * @return
     */
    public DrpcBootstrap registry(ResgistryConfig registryConfig) {
        //创建zookeeper连接实例，ps耦合

        this.registry = registryConfig.getRegistry();
        return this;

    }

    /**
     * 初始序列化,配置当前暴露的服务所使用的协议
     * @param protocolConfig
     * @return
     */
    public DrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if (log.isInfoEnabled()) {
            log.debug("序列化协议:{}，已经被注册", protocolConfig.toString());
        }
        return this;
    }
    /**
     * 发布服务,将接口的实现类发布到注册中心
     * @param service 封装的需要服务
     * @return this
     */
    public DrpcBootstrap publish(ServiceConfig<?> service) {
        //抽象注册中心的概念，使用注册中心的实现完成实现
        registry.register(service);
        return this;
    }

    /**
     * 批量发布服务
     * @param services 封装的需要服务的集合
     * @return this
     */
    public DrpcBootstrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services) {
            publish(service);
        }
        return this;
    }

    /**
     * 启动服务
     */
    public void start() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * -----------------------------------------------服务消费方api-------------------------------------------------------
     */

    /**
     * 引用服务
     * @param reference
     * @return this
     */
    public DrpcBootstrap reference(ReferenceConfig<?> reference) {
        return this;
    }
}
