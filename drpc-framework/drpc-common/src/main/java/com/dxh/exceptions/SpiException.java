package com.dxh.exceptions;

/**
 * 服务与注册中心发现异常
 */
public class SpiException extends RuntimeException {
    public SpiException() {
    }

    public SpiException(String message) {
        super(message);
    }
    public SpiException(Throwable cause) {
        super(cause);
    }
}
