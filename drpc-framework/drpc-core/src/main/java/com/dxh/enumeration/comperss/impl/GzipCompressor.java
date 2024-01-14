package com.dxh.enumeration.comperss.impl;

import com.dxh.enumeration.comperss.Compressor;
import com.dxh.exceptions.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 使用gzip压缩的具体实现
 */
@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos)
        ){
            gzip.write(bytes);
            gzip.finish();
            byte[] result = baos.toByteArray();
            if (log.isDebugEnabled()){
                log.debug("gzip compress success, origin length:[{}], compress length:[{}]", bytes.length, result.length);
            }
            return result;
        }catch (IOException e) {
            log.error("gzip compress error", e);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try(
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                GZIPInputStream gzipInputStream = new GZIPInputStream(bais);
        ){
            byte[] result = gzipInputStream.readAllBytes();
            if (log.isDebugEnabled()){
                log.debug("gzip decompress success, compress length:[{}], decompress or origin length:[{}]", bytes.length,result.length);
            }
            return result;
        }catch (IOException e) {
            log.error("gzip decompress error", e);
            throw new CompressException(e);
        }
    }
}
