package com.dxh.exceptions;

/**
 * 服务与注册中心发现异常
 */
public class SerializeException extends RuntimeException {
    public SerializeException() {
    }

    public SerializeException(String message) {
        super(message);
    }
    public SerializeException(Throwable cause) {
        super(cause);
    }
}
