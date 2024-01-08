package com.dxh.serialize;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Slf4j
public class SerializeUtils {
    public static byte[] serialize(Object object) {
        if (object == null){
            return null;
        }
        //对payload这个对象进行序列化和压缩
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);
            outputStream.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化失败:{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
