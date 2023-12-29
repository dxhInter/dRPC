package com.dxh.netty;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class WatcherTest implements Watcher {

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.None){
            if(watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                System.out.println("connected");
            } else if (watchedEvent.getState() == Event.KeeperState.Disconnected){
                System.out.println("disconnected");
            } else if (watchedEvent.getState() == Event.KeeperState.Expired){
                System.out.println("expired");
            } else if (watchedEvent.getState() == Event.KeeperState.AuthFailed){
                System.out.println("auth failed");
            }
        } else if (watchedEvent.getType() == Event.EventType.NodeCreated) {
            System.out.println("node created");
        } else if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
            System.out.println("node deleted");
        } else if (watchedEvent.getType() == Event.EventType.NodeDataChanged) {
            System.out.println("node data changed");
        }
    }
}
