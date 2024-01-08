package com.dxh.exceptions;

/**
 * 服务与注册中心发现异常
 */
public class CompressException extends RuntimeException {
    public CompressException() {
    }

    public CompressException(String message) {
        super(message);
    }
    public CompressException(Throwable cause) {
        super(cause);
    }
}
