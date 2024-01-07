package com.dxh.transport.message;

public class MessageFormatConstant {
    public final static byte[] MAGIC = "drpc".getBytes();
    public final static byte VERSION = 1;
    //计算头部字节的长度
    public final static int HEADER_FIELD_LENGTH = 2;
    //头部内容的真实长度
    public final static short HEADER_LENGTH = (byte)(MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1 + 8);
    public final static int MAX_FRAME_LENGTH = 1024 * 1024;
    public final static int VERSION_LENGTH = 1;
    //总长度的字节数
    public static final int FULL_FIELD_LENGTH = 4;
}
