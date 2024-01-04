package com.dxh;

import com.dxh.utils.NetUtils;
import com.dxh.utils.zookeeper.ZookeeperNode;
import com.dxh.utils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

@Slf4j
public class DrpcBootstrap {
    // DrpcBootstrap 是一个单例，饿汉式
    private static final DrpcBootstrap drpcBootstrap = new DrpcBootstrap();

    //定义相关基础配置
    private String applicationName = "default";
    private ResgistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private ZooKeeper zooKeeper;

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
        //TODO 优化耦合
        zooKeeper = ZookeeperUtils.createZookeeper();

        this.registryConfig = registryConfig;
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
        //服务名称的节点，持久节点
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + service.getInterface().getName();

        if (!ZookeeperUtils.exists(zooKeeper, parentNode,null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);
        }

        //创建本机的临时节点
        String node = parentNode + "/" + NetUtils.getIp() + ":" + port;
//        String node = parentNode + "/" + "172.20.10.12" + ":" + port;
        if (!ZookeeperUtils.exists(zooKeeper, node,null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(node, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
        }

        if (log.isInfoEnabled()) {
            log.debug("node is :{}", node);
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
