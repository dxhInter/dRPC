package com.dxh.utils.zookeeper;

import com.dxh.Constant;
import com.dxh.exceptions.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZookeeperUtils {
    /**
     * 使用默认的连接字符串和会话超时时间创建zookeeper实例
     * @return
     */
    public static ZooKeeper createZookeeper(){
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int sessionTimeout = Constant.ZK_SESSION_TIMEOUT;
        return createZookeeper(connectString, sessionTimeout);
    }
    /**
     * 使用指定的连接字符串和会话超时时间创建zookeeper实例
     * @param connectString
     * @param sessionTimeout
     * @return
     */
    public static ZooKeeper createZookeeper(String connectString, int sessionTimeout) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            //创建zookeeper连接实例
            final ZooKeeper zooKeeper = new ZooKeeper(connectString, sessionTimeout, event -> {
                //only connect event is sync connected, we can create node
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.debug("zookeeper connected successfully");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            //定义节点数据
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
            log.error("create zookeeper instance error: ", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 创建节点
     * @param zooKeeper
     * @param node
     * @param watcher
     * @param createMode
     * @return true if create successfully, false if node already exists
     */
    public static Boolean createNode(ZooKeeper zooKeeper, ZookeeperNode node, Watcher watcher, CreateMode createMode){
        try {
            if(zooKeeper.exists(node.getNodePath(), null) == null) {
                String result = zooKeeper.create(node.getNodePath(), node.getData(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("node {}, create successfully", result);
                return true;
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("node {} already exists", node.getNodePath());
                }
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("create base nodes error: ", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 检查节点是否存在
     * @param zooKeeper
     * @param node
     * @param watcher
     * @return true if node exists, false if node not exists
     */
    public static Boolean exists(ZooKeeper zooKeeper, String node, Watcher watcher){
        try {
            return zooKeeper.exists(node, watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            log.error("check node[{}] exists error: ",node,e);
            throw new ZookeeperException(e);
        }
    }

    /**
     * 关闭zookeeper连接
     * @param zooKeeper
     */
    public static void close(ZooKeeper zooKeeper){
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("close zookeeper error: ", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 获取子节点
     * @param zooKeeper zookeeper实例
     * @param serviceNode 服务节点
     * @return
     */
    public static List<String> getChildren(ZooKeeper zooKeeper, String serviceNode, Watcher watcher) {
        try {
            return zooKeeper.getChildren(serviceNode, watcher);
        } catch (KeeperException | InterruptedException e) {
            log.error("get a node's children error: ", e,serviceNode);
            throw new ZookeeperException(e);
        }
    }
}
