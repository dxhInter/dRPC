package com.dxh.protection;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 熔断器
 */
public class Breaker {
    //标准断路器有三种状态：open、close、half-open
    private volatile boolean isOpen = false;
    //总的请求数
    private AtomicInteger requestCount = new AtomicInteger(0);
    //失败异常的请求数
    private AtomicInteger errorRequest = new AtomicInteger(0);
    //异常阈值
    private int maxErrorRequest;
    //异常比例阈值
    private float maxErrorRate;

    public Breaker(int maxErrorRequest, float maxErrorRate) {
        this.maxErrorRequest = maxErrorRequest;
        this.maxErrorRate = maxErrorRate;
    }

    /**
     * 判断是否打开断路器
     * @return true 打开，false 没有打开
     */
    public boolean isBreak(){
        //如果已经打开，那么就直接返回
        if (isOpen){
            return true;
        }
        //如果失败的请求数大于异常阈值，那么就打开断路器
        if (errorRequest.get() > maxErrorRequest){
            this.isOpen = true;
            return true;
        }
        //如果异常比例小于异常比例阈值，那么就直接返回
        if (errorRequest.get() > 0 && requestCount.get() > 0 &&
                (errorRequest.get() / (float)requestCount.get()) > maxErrorRate){
            this.isOpen = true;
            return true;
        }
        return false;
    }

    //每次发生请求，记录
    public void recordRequest(){
        this.requestCount.getAndIncrement();
    }
    //统计失败的请求
    public void recordErrorRequest(){
        this.errorRequest.getAndIncrement();
    }

    /**
     * 重置断路器
     */
    public void reset(){
        this.isOpen = false;
        this.requestCount.set(0);
        this.errorRequest.set(0);
    }

    public static void main(String[] args) {
        Breaker breaker = new Breaker(3,0.2f);
        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                breaker.recordRequest();
                int nextInt = new Random().nextInt(100);
                if (nextInt > 70){
                    breaker.recordErrorRequest();
                }
                boolean aBreak = breaker.isBreak();
                String result = aBreak ? "阻塞" : "放行";
                System.out.println("result = " + result);
                System.out.println("aBreak is :"+aBreak);
            }
        }).start();
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            breaker.reset();
        }).start();
    }
}