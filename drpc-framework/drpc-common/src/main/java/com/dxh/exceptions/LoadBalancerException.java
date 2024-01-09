package com.dxh.exceptions;

/**
 * 服务与注册中心发现异常
 */
public class LoadBalancerException extends RuntimeException {
    public LoadBalancerException() {
    }

    public LoadBalancerException(String message) {
        super(message);
    }
    public LoadBalancerException(Throwable cause) {
        super(cause);
    }
}
