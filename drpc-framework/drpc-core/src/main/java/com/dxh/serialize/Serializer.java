package com.dxh.serialize;

/**
 * 序列化接口
 * @author
 *
 */
public interface Serializer {
    /**
     * 序列化
     * @param object
     * @return byte[]
     */
    byte[] serialize(Object object);

    /**
     * 反序列化
     * @param bytes
     * @param clazz 目标类对象
     * @return T
     * @param <T>
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
