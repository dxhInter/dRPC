package com.dxh;

import com.dxh.netty.WatcherTest;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZookeeperTest {
    ZooKeeper zooKeeper;
    CountDownLatch countDownLatch = new CountDownLatch(1);

    @Before
    public void createZK() throws IOException {
        String connectString = "192.168.101.129:2181,192.168.101.129:2181,192.168.101.129:2181";
        int sessionTimeout = 10000;

//        zooKeeper = new ZooKeeper(connectString, sessionTimeout, new WatcherTest());
        zooKeeper = new ZooKeeper(connectString, sessionTimeout, event -> {
            //only connect event is sync connected, we can create node
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                System.out.println("connected");
                countDownLatch.countDown();
            }
        });
    }

    @Test
    public void testCreatePersistNode()  {
        String result = null;
        try {
            countDownLatch.await();
            result = zooKeeper.create("/dxh", "dxh".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println(result);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            if (zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void testDeletePersistNode()  {
        String result = null;
        try {
            //version is -1, delete all version
            zooKeeper.delete("/dxh",-1);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void testExistPersistNode()  {
        try {
            //version is -1, delete all version
            Stat stat = zooKeeper.exists("/dxh", true);
            zooKeeper.setData("/dxh","hello".getBytes(),-1);
            int version = stat.getVersion();
            System.out.println("version = " + version);
            int aversion = stat.getAversion();
            System.out.println("aversion = " + aversion);
            int cversion = stat.getCversion();
            System.out.println("cversion = " + cversion);

        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void tesWatcher()  {
        try {
            //version is -1, delete all version
            zooKeeper.exists("/dxh", true);
            while (true){
                Thread.sleep(10000);
            }
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
