package com.dxh.exceptions;

/**
 * 服务与注册中心发现异常
 */
public class ResponseException extends RuntimeException {
    private byte code;
    private String msg;
    public ResponseException(byte code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
