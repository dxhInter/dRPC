package com.dxh.comperss;

public interface Compressor {
    /**
     * 对字节数组压缩
     * @param bytes
     * @return
     */
    byte[] compress(byte[] bytes);
    /**
     * 对字节数组解压
     * @param bytes
     * @return
     */
    byte[] decompress(byte[] bytes);
}
