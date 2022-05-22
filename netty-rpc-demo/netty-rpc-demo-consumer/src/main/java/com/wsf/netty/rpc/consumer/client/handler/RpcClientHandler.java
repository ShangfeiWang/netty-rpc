package com.wsf.netty.rpc.consumer.client.handler;

import com.wsf.netty.rpc.common.codec.RpcResponse;
import com.wsf.netty.rpc.common.protocol.RpcProtocol;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private RpcProtocol rpcProtocol;

    private volatile Channel channel;

    //private ConcurrentHashMap<String, RpcFuture> pendingRPC = new ConcurrentHashMap<>();

    public void setRpcProtocol(RpcProtocol rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        String requestId = rpcResponse.getRequestId();
        log.info("receive response : {}", requestId);

    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
