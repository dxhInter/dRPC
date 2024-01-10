package com.dxh.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务提供方回复的响应内容
 *  DrpcResponse
 *  1、请求id
 *  2、压缩类型 (1byte)
 *  3、序列化的方式(1byte)
 *  4、消息类型(普通请求,心跳检测请求)
 *  5、负载 payload(接口的名字,方法的名字,参数列表,返回值类型)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DrpcResponse {
    private long requestId;
    //压缩类型
    private byte compressType;
    //序列化类型
    private byte serializerType;
    private long timeStamp;

    //1成功,   2失败
    private byte code;

    //消息体
    private Object body;


}
