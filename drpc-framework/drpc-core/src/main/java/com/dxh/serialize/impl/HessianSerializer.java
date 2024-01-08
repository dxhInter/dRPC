package com.dxh.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.dxh.exceptions.SerializeException;
import com.dxh.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null){
            return null;
        }

        try(
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ) {
                Hessian2Output hessian2Output = new Hessian2Output(baos);
                hessian2Output.writeObject(object);
                hessian2Output.flush();
                if (log.isDebugEnabled()){
                    log.debug("serialize object [{}] success by using Hessian", object);
                }
                return baos.toByteArray();
        } catch (IOException e) {
            log.error("using Hessian to serialize object [{}] have error", object);
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

        ) {
            Hessian2Input hessian2Input = new Hessian2Input(bais);
            T t = (T) hessian2Input.readObject();
            if (log.isDebugEnabled()){
                log.debug("deserialize class [{}] success by using Hessian", clazz);
            }
            return t;
        } catch (IOException e) {
            log.error("by using Hessian to deserialize object [{}] have error", clazz);
            throw new SerializeException(e);
        }
    }
}
