package com.dxh.channelhandler.handler;

import com.dxh.enumeration.comperss.Compressor;
import com.dxh.enumeration.comperss.CompressorFactory;
import com.dxh.serialize.Serializer;
import com.dxh.serialize.SerializerFactory;
import com.dxh.transport.message.DrpcRequest;
import com.dxh.transport.message.MessageFormatConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 4B magic(魔术值) ---drpc.getBytes()
 * 1B version(版本号) ---1
 * 2B header Length(头部长度)
 * 4B full content Length(内容长度)
 * 1B serialize type(序列化类型)
 * 1B compress type(压缩类型)
 * 1B request type(请求类型)
 * 8B request id(请求id)
 * 编码器
 * 出站是的第一个处理器
 */
@Slf4j
public class DrpcRequestEncoder extends MessageToByteEncoder<DrpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, DrpcRequest drpcRequest, ByteBuf byteBuf) throws Exception {
        //1. 魔术值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        //2. 版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        //3. 头部长度(两个字节的头部)
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        //4. 内容长度为4个字节，先将写指针条过4个字节，等到写完内容后再回来写入
        byteBuf.writerIndex(byteBuf.writerIndex()+MessageFormatConstant.FULL_FIELD_LENGTH);
        //3 个类型
        byteBuf.writeByte(drpcRequest.getRequestType());
        byteBuf.writeByte(drpcRequest.getSerializerType());
        byteBuf.writeByte(drpcRequest.getCompressType());
        //8. 8字节请求id
        byteBuf.writeLong(drpcRequest.getRequestId());
        byteBuf.writeLong(drpcRequest.getTimeStamp());
        //如果是心跳请求，不需要写入请求体
//        if(drpcRequest.getRequestType() == RequestType.HEARTBEAT.getId()){
//            return;
//        }
        //9. 写入请求体
        byte[] body = null;
        //根据请求体里设置的序列化类型进行序列化
        if (drpcRequest.getPayload() != null){
            Serializer serializer = SerializerFactory.getSerializer(drpcRequest.getSerializerType()).getSerializer();
            body = serializer.serialize(drpcRequest.getPayload());
            //根据请求体里设置的压缩类型进行压缩
            Compressor compressor = CompressorFactory.getCompressor(drpcRequest.getCompressType()).getCompressor();
            body = compressor.compress(body);
        }


        if (body != null){
            byteBuf.writeBytes(body);
        }
        int bodyLength = body == null ? 0 : body.length;
        //保存当前写指针位置
        int writerIndex = byteBuf.writerIndex();
        //重新设置内容长度, 7是魔术值+版本号+头部长度
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        //重新设置写指针，并归位
        byteBuf.writerIndex(writerIndex);

        if(log.isDebugEnabled()){
            log.debug("request encode success, requestId:{}", drpcRequest.getRequestId());
        }
    }
}
