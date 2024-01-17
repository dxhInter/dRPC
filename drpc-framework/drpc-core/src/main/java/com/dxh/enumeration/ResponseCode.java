package com.dxh.enumeration;

/**
 * 响应状态码
 * 1. 成功 20(方法成功调用) 21(心跳成功)
 * 2. 错误吗 服务端 50(方法调用不存在)
 *         客户端 44(方法调用失败)
 * 3. 负载码 31(服务器负载过高，限流)
 */
public enum ResponseCode {
    SUCCESS((byte) 20,"成功"),
    SUCCESS_HEARTBEAT((byte) 21,"心跳成功"),
    RATE_LIMITING((byte) 31,"服务被限流"),
    RESOURCE_NOT_FOUND((byte) 44,"请求的资源不存在"),
    FAIL((byte) 50,"调用方法失败");
    private byte code;
    private String desc;

    ResponseCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
