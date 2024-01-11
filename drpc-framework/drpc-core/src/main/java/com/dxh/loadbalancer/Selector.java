package com.dxh.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡器查找接口
 */
public interface Selector {
    /**
     * 根据服务列表执行某个算法获取一个可用服务
     * @return
     */
    InetSocketAddress getNext();

}
