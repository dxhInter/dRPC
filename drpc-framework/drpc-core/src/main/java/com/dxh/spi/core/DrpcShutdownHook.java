package com.dxh.spi.core;

/**
 *
 */
public class DrpcShutdownHook extends Thread{
    @Override
    public void run() {
        // 1 打开挡板 boolean 线程安全
        ShotDownHolder.BAFFLE.set(true);

        // 2 等待计数器归零 正常请求结束
        long startTime = System.currentTimeMillis();
        while (true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (ShotDownHolder.REQUEST_COUNTER.sum() == 0L || System.currentTimeMillis() - startTime > 10000){
                break;
            }
        }
        // 等待归零，继续执行 countDownLatch

        // 3 阻塞结束后 放行
    }
}
