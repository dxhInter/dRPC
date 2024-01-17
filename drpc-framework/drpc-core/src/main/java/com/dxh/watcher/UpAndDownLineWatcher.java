package com.dxh.watcher;

import com.dxh.DrpcBootstrap;
import com.dxh.NettyBootstrapInitializer;
import com.dxh.discovery.Registry;
import com.dxh.loadbalancer.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import static com.caucho.services.server.ServiceContext.getServiceName;

@Slf4j
public class UpAndDownLineWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
            if(log.isDebugEnabled()){
                log.debug("检测到节点上下线:{}",watchedEvent);
            }
            Registry registry = DrpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
            String serviceName = getServiceName(watchedEvent.getPath());
            List<InetSocketAddress> addresses = registry.lookup(serviceName,DrpcBootstrap.getInstance().getConfiguration().getGroup());
            //处理新增的节点
            for (InetSocketAddress address : addresses) {
                //新增的节点 可能在address中，但是在channelCache中没有
                //下线的节点 可能在channelCache中，但是在address中没有
                if(!DrpcBootstrap.CHANNEL_CACHE.containsKey(address)){
                    //根据地址建立连接
                    Channel channel = null;
                    try {
                        channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    DrpcBootstrap.CHANNEL_CACHE.put(address,channel);
                }
            }
            //处理下线的节点
            for (Map.Entry<InetSocketAddress,Channel> entry : DrpcBootstrap.CHANNEL_CACHE.entrySet()){
                if(!addresses.contains(entry.getKey())) {
                    //关闭连接
                    DrpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                }
            }

            LoadBalancer loadBalancer = DrpcBootstrap.getInstance().getConfiguration().getLoadBalancer();
            loadBalancer.reLoadBalance(serviceName,addresses);
        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
