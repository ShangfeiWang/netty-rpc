package com.wsf.netty.rpc.consumer.client.handler;

import com.wsf.netty.rpc.common.codec.Beat;
import com.wsf.netty.rpc.common.codec.RpcDecoder;
import com.wsf.netty.rpc.common.codec.RpcEncoder;
import com.wsf.netty.rpc.common.codec.RpcRequest;
import com.wsf.netty.rpc.common.codec.RpcResponse;
import com.wsf.netty.rpc.common.serializer.Serializer;
import com.wsf.netty.rpc.common.serializer.impl.JsonSerializer;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class RpcClientInitializer extends ChannelInitializer<NioSocketChannel> {

    @Override
    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
        // todo 添加handler
        Serializer jsonSerializer = new JsonSerializer();
        ChannelPipeline pipeline = nioSocketChannel.pipeline();
        pipeline.addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS));
        pipeline.addLast(new ChannelDuplexHandler() {
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                if (evt instanceof IdleStateEvent) {
                    RpcRequest rpcRequest = new RpcRequest();
                    rpcRequest.setRequestId(Beat.BEAT_ID);
                    try {
                        ChannelFuture sync = ctx.channel().writeAndFlush(rpcRequest).sync();
                        if (!sync.isSuccess()) {
                            log.error("send ping request error requestId:{}", rpcRequest.getRequestId());
                        }
                    } catch (InterruptedException e) {
                        log.error("send ping request error message:{}", e.getMessage());
                    }
                } else {
                    super.userEventTriggered(ctx, evt);
                }
            }
        });
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 0));
        pipeline.addLast(new RpcEncoder(RpcRequest.class, jsonSerializer));
        pipeline.addLast(new RpcDecoder(RpcResponse.class, jsonSerializer));
        pipeline.addLast(new RpcClientHandler());
    }
}
