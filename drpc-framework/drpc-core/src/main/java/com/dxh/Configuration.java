package com.dxh;

import com.dxh.loadbalancer.LoadBalancer;
import com.dxh.loadbalancer.impl.RoundRobinLoadBalancer;
import lombok.Data;

/**
 * 全局的配置信息, 代码配置-->xml配置-->spi配置-->默认配置
 */
@Data
public class Configuration {
    //配置信息 -> 服务端口号
    private int port = 8083;
    //配置信息 -> 应用名称
    private String applicationName = "default";
    //配置信息 -> 注册中心
    private RegistryConfig registryConfig;
    //配置信息 -> 序列化协议
    private ProtocolConfig protocolConfig;
    //配置信息 -> 序列化类型
    private String serializeType = "jdk";
    //配置信息 -> 压缩类型
    private String compressType = "gzip";
    //配置信息 -> ID生成器
    private final IdGenerator idGenerator = new IdGenerator(1,2);
    //配置信息 -> 负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();


    // 读xml
    public Configuration() {
        //通过xml配置文件读取配置信息

    }
    // 进行配置

}
