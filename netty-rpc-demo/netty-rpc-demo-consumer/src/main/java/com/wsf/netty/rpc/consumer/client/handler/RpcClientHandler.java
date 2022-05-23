package com.wsf.netty.rpc.consumer.client.handler;

import com.wsf.netty.rpc.common.codec.RpcRequest;
import com.wsf.netty.rpc.common.codec.RpcResponse;
import com.wsf.netty.rpc.common.protocol.RpcProtocol;
import com.wsf.netty.rpc.consumer.client.connect.ConnectionManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private RpcProtocol rpcProtocol;

    private volatile Channel channel;

    private SocketAddress socketAddress;

    private ConcurrentHashMap<String, RpcFuture> pendingRPC = new ConcurrentHashMap<>();

    public void setRpcProtocol(RpcProtocol rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.socketAddress = ctx.channel().remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        String requestId = rpcResponse.getRequestId();
        log.info("receive response : {}", requestId);
        RpcFuture rpcFuture = pendingRPC.get(requestId);
        if (rpcFuture != null) {
            pendingRPC.remove(requestId);
            rpcFuture.done(rpcResponse);
        } else {
            log.warn("Can not get pending response for request id: " + requestId);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ConnectionManager.getInstance().removeHandler(rpcProtocol);
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public RpcFuture sendRpcRequest(RpcRequest rpcRequest) {
        RpcFuture rpcFuture = new RpcFuture(rpcRequest);
        pendingRPC.put(rpcRequest.getRequestId(), rpcFuture);
        try {
            ChannelFuture channelFuture = channel.writeAndFlush(rpcRequest).sync();
            if (!channelFuture.isSuccess()) {
                log.error("Send request {} error", rpcRequest.getRequestId());
            }
        } catch (InterruptedException e) {
            log.error("Send request exception: " + e.getMessage());
        }
        return rpcFuture;
    }
}
