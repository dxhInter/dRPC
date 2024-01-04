package com.dxh;

public class Constant {
    // 默认的zookeeper地址
    public static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";

    // 默认的zookeeper会话超时时间
    public static final int ZK_SESSION_TIMEOUT = 10000;

    // 服务提供者和消费者的基础路径
    public static final String BASE_PROVIDER_PATH = "/drpc-metadata/providers";
    public static final String BASE_CONSUMER_PATH = "/drpc-metadata/consumers";
}
