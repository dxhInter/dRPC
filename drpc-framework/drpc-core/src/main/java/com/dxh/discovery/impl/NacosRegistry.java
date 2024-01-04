package com.dxh.discovery.impl;

import com.dxh.Constant;
import com.dxh.ServiceConfig;
import com.dxh.discovery.AbstractRegistry;
import com.dxh.utils.NetUtils;
import com.dxh.utils.zookeeper.ZookeeperNode;
import com.dxh.utils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

@Slf4j
public class NacosRegistry extends AbstractRegistry {
    private ZooKeeper zooKeeper;

    public NacosRegistry() {
        this.zooKeeper = ZookeeperUtils.createZookeeper();
    }

    public NacosRegistry(String connectString, int sessionTimeout) {
        this.zooKeeper = ZookeeperUtils.createZookeeper(connectString, sessionTimeout);
    }

    @Override
    public void register(ServiceConfig<?> service) {
        String parentNode = Constant.BASE_PROVIDER_PATH + "/" + service.getInterface().getName();

        if (!ZookeeperUtils.exists(zooKeeper, parentNode,null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);
        }

        //创建本机的临时节点
        //todo 解决端口的问题
        String node = parentNode + "/" + NetUtils.getIp() + ":" + 8082;
//        String node = parentNode + "/" + "172.20.10.12" + ":" + port;
        if (!ZookeeperUtils.exists(zooKeeper, node,null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(node, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
        }

        if (log.isInfoEnabled()) {
            log.debug("node is :{}", node);
            log.debug("服务:{}，已经被注册", service.getInterface().getName());
        }
    }
}
