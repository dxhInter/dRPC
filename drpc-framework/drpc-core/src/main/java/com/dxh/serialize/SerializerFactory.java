package com.dxh.serialize;

import com.dxh.serialize.impl.HessianSerializer;
import com.dxh.serialize.impl.JdkSerializer;
import com.dxh.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;


/**
 * 序列化工厂类
 */
@Slf4j
public class SerializerFactory {
    private final static ConcurrentHashMap<String,SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Byte,SerializerWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>();
    static {
        SerializerWrapper jdk = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializerWrapper json = new SerializerWrapper((byte) 2, "json", new JsonSerializer());
        SerializerWrapper hessian = new SerializerWrapper((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE.put("jdk",jdk);
        SERIALIZER_CACHE.put("json",json);
        SERIALIZER_CACHE.put("hessian",hessian);
        SERIALIZER_CACHE_CODE.put((byte) 1,jdk);
        SERIALIZER_CACHE_CODE.put((byte) 2,json);
        SERIALIZER_CACHE_CODE.put((byte) 3,hessian);
    }

    /**
     * 根据序列化类型获取序列化实例
     * @param serializeType
     * @return SerializerWrapper
     */
    public static SerializerWrapper getSerializer(String serializeType) {
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE.get(serializeType);
        if (serializerWrapper == null) {
            if (log.isInfoEnabled()) {
                log.error("serializeType [{}] is not found, use default jdk", serializeType);
            }
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }

    /**
     * 根据序列化编码获取序列化实例
     * @param serializeCode
     * @return
     */
    public static SerializerWrapper getSerializer(Byte serializeCode) {
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        if (serializerWrapper == null) {
            if (log.isInfoEnabled()) {
                log.error("serializeCode [{}] is not found, use default jdk", serializeCode);
            }
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }
}
