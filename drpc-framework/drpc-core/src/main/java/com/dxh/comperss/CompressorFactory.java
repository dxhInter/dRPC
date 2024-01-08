package com.dxh.comperss;

import com.dxh.comperss.impl.GzipCompressor;
import com.dxh.serialize.SerializerWrapper;
import com.dxh.serialize.impl.HessianSerializer;
import com.dxh.serialize.impl.JdkSerializer;
import com.dxh.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化工厂类
 */
@Slf4j
public class CompressorFactory {
    private final static ConcurrentHashMap<String, CompressorWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Byte,CompressorWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>();
    static {
        CompressorWrapper gzip = new CompressorWrapper((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip",gzip);
        COMPRESSOR_CACHE_CODE.put((byte) 1,gzip);
    }

    /**
     * 根据压缩类型获取压缩实例
     * @param compressorType
     * @return SerializerWrapper
     */
    public static CompressorWrapper getCompressor(String compressorType) {
        CompressorWrapper compressorWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (compressorWrapper == null) {
            if (log.isDebugEnabled()){
                log.debug("compressorType [{}] is not found, use default gzip", compressorType);
            }
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorWrapper;
    }

    /**
     * 根据压缩编码获取压缩实例
     * @param compressorCode
     * @return
     */
    public static CompressorWrapper getCompressor(byte compressorCode) {
       CompressorWrapper compressorWrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
         if (compressorWrapper == null) {
            if (log.isDebugEnabled()){
                log.debug("compressorCode [{}] is not found, use default gzip", compressorCode);
            }
             return COMPRESSOR_CACHE.get("gzip");
         }
        return compressorWrapper;
    }
}
