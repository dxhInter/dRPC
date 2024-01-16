package com.dxh.comperss;

import com.dxh.comperss.impl.GzipCompressor;
import com.dxh.config.ObjectWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化工厂类
 */
@Slf4j
public class CompressorFactory {
    private final static Map<String, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>();
    private final static Map<Byte,ObjectWrapper<Compressor>> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>();
    static {
        ObjectWrapper<Compressor> gzip = new ObjectWrapper<>((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip",gzip);
        COMPRESSOR_CACHE_CODE.put((byte) 1,gzip);
    }

    /**
     * 根据压缩类型获取压缩实例
     * @param compressorType
     * @return SerializerWrapper
     */
    public static ObjectWrapper<Compressor> getCompressor(String compressorType) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (compressorObjectWrapper == null) {
            if (log.isDebugEnabled()){
                log.debug("compressorType [{}] is not found, use default gzip", compressorType);
            }
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorObjectWrapper;
    }

    /**
     * 根据压缩编码获取压缩实例
     * @param compressorCode
     * @return
     */
    public static ObjectWrapper<Compressor> getCompressor(byte compressorCode) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
         if (compressorObjectWrapper == null) {
            if (log.isDebugEnabled()){
                log.debug("compressorCode [{}] is not found, use default gzip", compressorCode);
            }
             return COMPRESSOR_CACHE.get("gzip");
         }
        return compressorObjectWrapper;
    }

    /**
     * 给工厂中新增一个压缩类型包装
     * @param compressorObjectWrapper
     * @return
     */
    public static void addCompressor(ObjectWrapper<Compressor> compressorObjectWrapper){
        COMPRESSOR_CACHE.put(compressorObjectWrapper.getName(),compressorObjectWrapper);
        COMPRESSOR_CACHE_CODE.put(compressorObjectWrapper.getCode(),compressorObjectWrapper);
    }

}
