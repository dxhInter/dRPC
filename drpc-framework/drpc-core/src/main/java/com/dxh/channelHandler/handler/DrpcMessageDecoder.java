package com.dxh.channelHandler.handler;

import com.dxh.enumeration.RequestType;
import com.dxh.transport.message.DrpcRequest;
import com.dxh.transport.message.MessageFormatConstant;
import com.dxh.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
@Slf4j
public class DrpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    public DrpcMessageDecoder() {
        super(
                // 找到当前报文的总长度，截取报文，截取出来的报文我们可以去进行解析
                // 最大帧的长度，超过这个maxFrameLength值会直接丢弃
                MessageFormatConstant.MAX_FRAME_LENGTH,
                // 长度的字段的偏移量，
                MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH,
                // 长度的字段的长度
                MessageFormatConstant.FULL_FIELD_LENGTH,
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH),
                0);
    }



    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf byteBuf){
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        // 1,解析魔术值
        byte [] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        //检测魔术值是否匹配
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageFormatConstant.MAGIC[i]){
                throw new RuntimeException("magic value is invalid");
            }
        }
        // 2,解析版本号
        byte version = byteBuf.readByte();
        if (version > MessageFormatConstant.VERSION){
            throw new RuntimeException("version is not support");
        }
        // 3,解析头部长度
        short headerLength = byteBuf.readShort();

        // 4,解析总内容长度
        int fullLength = byteBuf.readInt();

        // 5,解析请求类型 todo,这里的请求类型是否是心跳请求
        byte requestType = byteBuf.readByte();

        // 6,解析序列化类型
        byte serializerType = byteBuf.readByte();

        // 7,解析压缩类型
        byte compressType = byteBuf.readByte();

        // 8,解析请求id
        long requestId = byteBuf.readLong();

        // 封装
        DrpcRequest drpcRequest = new DrpcRequest();
        drpcRequest.setRequestType(requestType);
        drpcRequest.setCompressType(compressType);
        drpcRequest.setSerializerType(serializerType);

        //心跳请求直接返回
        if (requestType == RequestType.HEARTBEAT.getId()){
            return drpcRequest;
        }


        int payloadLength = fullLength - headerLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);
        log.info("payload is :{}", payload);

        //todo 解压缩和反序列化
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(payload);
            ois = new ObjectInputStream(bais);
            RequestPayload requestPayload = (RequestPayload)ois.readObject();
            drpcRequest.setPayload(requestPayload);
        } catch (IOException | ClassNotFoundException e) {
            log.error("error when decode serialize:{}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                bais.close();
                ois.close();
            } catch (IOException e) {
                log.error("error when close stream:{}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return drpcRequest;
    }
}
