package com.wsf.netty.rpc.common.codec;

import com.wsf.netty.rpc.common.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    private Serializer serializer;

    public RpcEncoder(Class<?> genericClass, Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) {
        if (genericClass.isInstance(o)) {
            try {
                byte[] data = serializer.serialize(o);
                byteBuf.writeInt(data.length);
                byteBuf.writeBytes(data);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("序列化失败", e);
            }
        }
    }
}
