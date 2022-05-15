package com.wsf.netty.rpc.provider.server.handler;

import com.wsf.netty.rpc.common.codec.RpcDecoder;
import com.wsf.netty.rpc.common.codec.RpcEncoder;
import com.wsf.netty.rpc.common.codec.RpcRequest;
import com.wsf.netty.rpc.common.codec.RpcResponse;
import com.wsf.netty.rpc.common.serializer.Serializer;
import com.wsf.netty.rpc.common.serializer.impl.JsonSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author wsf
 * @since 20220526
 */
public class RpcServiceInitializer extends ChannelInitializer<NioSocketChannel> {

    private Map<String, Object> serviceMap;

    private ThreadPoolExecutor threadPoolExecutor;

    public RpcServiceInitializer(Map<String, Object> serviceMap, ThreadPoolExecutor threadPoolExecutor) {
        this.serviceMap = serviceMap;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
        Serializer jsonSerializer = new JsonSerializer();
        // 添加拆包粘包解码器
        // 参数1：一个数据包最大的长度  参数2：长度字段偏移量  参数3：长度字段占几个字节  参数4：长度的调整值  参数5：需要跳过几个字节数
        nioSocketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 0));
        nioSocketChannel.pipeline().addLast(new RpcDecoder(RpcRequest.class, jsonSerializer));
        nioSocketChannel.pipeline().addLast(new RpcEncoder(RpcResponse.class, jsonSerializer));
        nioSocketChannel.pipeline().addLast(new RpcServerHandler(serviceMap, threadPoolExecutor));
    }
}
