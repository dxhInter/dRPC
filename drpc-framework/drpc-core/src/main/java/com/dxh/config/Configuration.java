package com.dxh.config;

import com.dxh.IdGenerator;
import com.dxh.ProtocolConfig;
import com.dxh.RegistryConfig;
import com.dxh.comperss.Compressor;
import com.dxh.comperss.impl.GzipCompressor;
import com.dxh.loadbalancer.LoadBalancer;
import com.dxh.loadbalancer.impl.RoundRobinLoadBalancer;
import com.dxh.serialize.Serializer;
import com.dxh.serialize.impl.JdkSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局的配置信息, 代码配置-->xml配置-->spi配置-->默认配置
 */
@Data
@Slf4j
public class Configuration {
    //配置信息 -> 服务端口号
    private int port = 8083;
    //配置信息 -> 应用名称
    private String applicationName = "default";
    //配置信息 -> 注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");
    //配置信息 -> 序列化类型
    private String serializeType = "jdk";

    //配置信息 -> 压缩类型
    private String compressType = "gzip";

    //配置信息 -> ID生成器
    private IdGenerator idGenerator = new IdGenerator(1,2);
    //配置信息 -> 负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();



    // 读xml
    public Configuration() {
        //使用spi机制加载默认配置
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);

        //通过xml配置文件读取配置信息
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXml(this);
    }

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
    }

}
