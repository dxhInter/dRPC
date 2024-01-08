package com.dxh.serialize.impl;

import com.dxh.exceptions.SerializeException;
import com.dxh.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null){
            return null;
        }
        try(
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream outputStream = new ObjectOutputStream(baos);
            ) {
                outputStream.writeObject(object);
                if (log.isDebugEnabled()){
                    log.debug("serialize object {} success", object);
                }
                return baos.toByteArray();
        } catch (IOException e) {
            log.error("serialize object {} have error", object);
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null){
            return null;
        }
        try(
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(bais);
        ) {
            Object object = objectInputStream.readObject();
            if (log.isDebugEnabled()){
                log.debug("deserialize class {} success", clazz);
            }
            return (T)object;
        } catch (IOException | ClassNotFoundException e) {
            log.error("deserialize object {} have error", clazz);
            throw new SerializeException(e);
        }
    }
}
