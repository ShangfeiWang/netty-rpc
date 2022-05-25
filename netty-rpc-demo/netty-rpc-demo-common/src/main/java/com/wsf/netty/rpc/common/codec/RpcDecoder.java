package com.wsf.netty.rpc.common.codec;

import com.wsf.netty.rpc.common.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    private Serializer serializer;

    public RpcDecoder(Class<?> genericClass, Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {

        if (byteBuf.readableBytes() < 4) {
            // 说明不是请求消息
            return;
        }
        int length = byteBuf.readInt();
        byte[] data = new byte[length];
        byteBuf.readBytes(data);
        Object o = null;
        try {
            o = serializer.deSerialize(data, genericClass);
        } catch (Exception e) {
            log.error("反序列化失败", e);
        }
        list.add(o);
    }
}
