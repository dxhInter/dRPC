package com.dxh.exceptions;

/**
 * 服务与注册中心发现异常
 */
public class DiscoveryException extends RuntimeException {
    public DiscoveryException() {
    }

    public DiscoveryException(String message) {
        super(message);
    }
    public DiscoveryException(Throwable cause) {
        super(cause);
    }
}
