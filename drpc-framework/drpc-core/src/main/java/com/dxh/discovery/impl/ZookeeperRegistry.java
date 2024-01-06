package com.dxh.discovery.impl;

import com.dxh.Constant;
import com.dxh.ServiceConfig;
import com.dxh.discovery.AbstractRegistry;
import com.dxh.exceptions.DiscoveryException;
import com.dxh.exceptions.NetworkException;
import com.dxh.utils.NetUtils;
import com.dxh.utils.zookeeper.ZookeeperNode;
import com.dxh.utils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
    private ZooKeeper zooKeeper;

    public ZookeeperRegistry() {
        this.zooKeeper = ZookeeperUtils.createZookeeper();
    }

    public ZookeeperRegistry(String connectString, int sessionTimeout) {
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

    @Override
    public InetSocketAddress lookup(String serviceName) {
        //找到服务对应的节点
        String serviceNode = Constant.BASE_PROVIDER_PATH + "/" + serviceName;
        //获取子节点
        List<String> children = ZookeeperUtils.getChildren(zooKeeper, serviceNode, null);

        //获取所有可用服务列表
        List<InetSocketAddress> collect = children.stream().map(child -> {
            String[] ipAndHost = child.split(":");
            String ip = ipAndHost[0];
            int port = Integer.parseInt(ipAndHost[1]);
            return new InetSocketAddress(ip, port);
        }).collect(java.util.stream.Collectors.toList());
        if (collect.size() == 0) {
            throw new DiscoveryException("没有可用的服务主机");
        }
        return collect.get(0);
    }
}