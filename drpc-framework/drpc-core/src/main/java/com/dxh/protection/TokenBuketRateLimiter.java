package com.dxh.protection;

import lombok.extern.slf4j.Slf4j;

/**
 * 令牌桶限流器
 * 令牌桶算法的原理是系统以恒定的速率产生令牌，然后把令牌放到令牌桶中，令牌桶有一个容量，当令牌桶满了的时候，再向其中放令牌，那么多余的令牌会被丢弃。
 */@Slf4j
public class TokenBuketRateLimiter implements RateLimiter{
    //令牌桶的数量,如果等于0，则阻拦所有请求
    private int tokens;
    //令牌桶的初始容量
    private final int capacity;
    //令牌桶的速率,每秒产生多少个令牌,500
    private final int rate;
    //上一次放token的时间
    private long lastTokenTime;

    public TokenBuketRateLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        lastTokenTime = System.currentTimeMillis();
        tokens = capacity;
    }

    /**
     * 判断是否允许请求
     * @return true 允许请求，false 不允许请求
     */
    public synchronized boolean allowRequest() {
        //给令牌桶添加令牌
        //计算两次请求的时间间隔，然后乘以速率，得到需要添加的令牌数量
        Long currentTime = System.currentTimeMillis();
        long intervalTime = currentTime - lastTokenTime;
        if (intervalTime >= (1000/rate)) {
            //如果时间间隔大于1秒，那么就需要添加令牌
            int needAddToken = (int) (intervalTime * rate / 1000);
            tokens = Math.min(capacity, tokens + needAddToken);
            //更新最后一次放token的时间
            this.lastTokenTime = System.currentTimeMillis();
        }
        //获取令牌，如果令牌数量大于0，则获取令牌
        if(tokens > 0){
            tokens--;
            return true;
        }else {
            return false;
        }
    }
}
