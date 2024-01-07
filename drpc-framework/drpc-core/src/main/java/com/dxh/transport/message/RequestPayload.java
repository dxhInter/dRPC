package com.dxh.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务调用方法起的请求内容
 * 接口的名字,方法的名字,参数列表,返回值类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestPayload implements Serializable {

    // 接口的名字, com.dxh.HelloDRPC
    private String interfaceName;
    // 方法的名字 sayHello
    private String methodName;
    //参数
    private Class<?>[] parameterTypes;
    private Object[] parameterValue;
    //参数类型
    //返回值类型
    private Class<?> returnType;
}
