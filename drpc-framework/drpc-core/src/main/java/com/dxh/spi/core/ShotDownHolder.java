package com.dxh.spi.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * 全局类，用于挡板的开启和关闭，以及计数器
 */
public class ShotDownHolder {
    // 挡板 boolean 线程安全 默认关闭
    public static AtomicBoolean BAFFLE = new AtomicBoolean(false);
    //用于请求的计数器
    public static LongAdder REQUEST_COUNTER = new LongAdder();
}
