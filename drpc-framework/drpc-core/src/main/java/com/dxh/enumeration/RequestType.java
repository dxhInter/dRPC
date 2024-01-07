package com.dxh.enumeration;

/**
 * 请求类型
 */
public enum RequestType {
    REQUEST((byte) 1,"commonRequest"),HEARTBEAT((byte) 2,"heartbeatRequest");
    private byte id;
    private String type;

    RequestType(byte id, String type) {
        this.id = id;
        this.type = type;
    }

    public byte getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
