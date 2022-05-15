package com.wsf.netty.rpc.provider.server.handler;

import com.wsf.netty.rpc.common.codec.RpcRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author wsf
 * @since 20220526
 */
@ChannelHandler.Sharable
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    public RpcServerHandler(Map<String, Object> serviceMap, ThreadPoolExecutor threadPoolExecutor) {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {

    }
}
