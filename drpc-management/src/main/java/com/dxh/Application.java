package com.dxh;

import com.dxh.exceptions.ZookeeperException;
import com.dxh.utils.zookeeper.ZookeeperNode;
import com.dxh.utils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Slf4j
public class Application {
    public static void main(String[] args) {
        //创建zookeeper连接实例
        ZooKeeper zooKeeper = ZookeeperUtils.createZookeeper();
        //定义节点数据
        String basePath = "/drpc-metadata";
        String providerPath = basePath + "/providers";
        String consumerPath = basePath + "/consumers";
        ZookeeperNode baseNode = new ZookeeperNode(basePath, null);
        ZookeeperNode providerNode = new ZookeeperNode(providerPath, null);
        ZookeeperNode consumerNode = new ZookeeperNode(consumerPath, null);

        //创建节点
        Arrays.asList(baseNode, providerNode, consumerNode).forEach(node -> {
            ZookeeperUtils.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
        });
        ZookeeperUtils.close(zooKeeper);
    }
}
