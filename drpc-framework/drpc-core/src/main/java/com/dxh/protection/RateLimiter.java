package com.dxh.protection;

/**
 * 限流器接口
 */
public interface RateLimiter {
    /**
     * 判断是否允许请求
     * @return true 允许请求，false 不允许请求
     */
    boolean allowRequest();
}
