package com.dxh.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.dxh.serialize.Serializer;
import com.dxh.transport.message.RequestPayload;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 使用JSON序列化,但是JSON序列化不支持序列化CLASS_FOR_NAME
 */

@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null){
            return null;
        }
        byte[] result = JSON.toJSONBytes(object);
        if (log.isDebugEnabled()){
            log.debug("serialize object [{}] success by using JSON", object);
        }
        return result;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null){
            return null;
        }
        T t = JSON.parseObject(bytes, clazz);
        if (log.isDebugEnabled()){
            log.debug("using JSON to deserialize class [{}] success", clazz);
        }
        return t;
    }

    public static void main(String[] args) {
        Serializer serializer = new JsonSerializer();
        RequestPayload requestPayload = new RequestPayload();
        requestPayload.setInterfaceName("com.dxh.HelloDRPC");
        requestPayload.setMethodName("sayHello");
        //JSON序列化不支持序列化CLASS_FOR_NAME
//        requestPayload.setReturnType(String.class);

        byte[] serialize = serializer.serialize(requestPayload);
        System.out.println(Arrays.toString(serialize));
        RequestPayload deserialize = serializer.deserialize(serialize, RequestPayload.class);
        System.out.println(deserialize);
    }
}
