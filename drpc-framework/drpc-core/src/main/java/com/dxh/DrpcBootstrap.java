package com.dxh;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class DrpcBootstrap {
    // DrpcBootstrap 是一个单例，饿汉式
    private static DrpcBootstrap drpcBootstrap = new DrpcBootstrap();

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
        return this;
    }

    /**
     * 配置注册中心
     * @param registryConfig
     * @return
     */
    public DrpcBootstrap registry(ResgistryConfig registryConfig) {
        return this;

    }

    /**
     * 初始序列化,配置当前暴露的服务所使用的协议
     * @param protocolConfig
     * @return
     */
    public DrpcBootstrap protocol(ProtocolConfig protocolConfig) {
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
        if (log.isInfoEnabled()) {
            log.debug("服务:{}，已经被注册", service.getInterface().getName());
        }
        return this;
    }

    /**
     * 批量发布服务
     * @param services 封装的需要服务的集合
     * @return this
     */
    public DrpcBootstrap publish(List<?> services) {
        return this;
    }

    /**
     * 启动服务
     */
    public void start() {
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
