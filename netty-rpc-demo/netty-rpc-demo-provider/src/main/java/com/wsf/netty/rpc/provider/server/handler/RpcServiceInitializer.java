package com.wsf.netty.rpc.provider.server.handler;

import com.wsf.netty.rpc.common.codec.Beat;
import com.wsf.netty.rpc.common.codec.RpcDecoder;
import com.wsf.netty.rpc.common.codec.RpcEncoder;
import com.wsf.netty.rpc.common.codec.RpcRequest;
import com.wsf.netty.rpc.common.codec.RpcResponse;
import com.wsf.netty.rpc.common.serializer.Serializer;
import com.wsf.netty.rpc.common.serializer.impl.JsonSerializer;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class RpcServiceInitializer extends ChannelInitializer<NioSocketChannel> {

    private Map<String, Object> serviceMap;

    private ThreadPoolExecutor threadPoolExecutor;

    private String applicationName;

    public RpcServiceInitializer(String applicationName, Map<String, Object> serviceMap, ThreadPoolExecutor threadPoolExecutor) {
        this.serviceMap = serviceMap;
        this.threadPoolExecutor = threadPoolExecutor;
        this.applicationName = applicationName;
    }

    @Override
    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
        Serializer jsonSerializer = new JsonSerializer();
        // 添加心跳包检查处理器
        nioSocketChannel.pipeline().addLast(new IdleStateHandler(0, 0, Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
        nioSocketChannel.pipeline().addLast(new ChannelDuplexHandler() {
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state() == IdleState.READER_IDLE) {
                    log.info("已经{}秒没有收到client发送过来心跳数据了", Beat.BEAT_TIMEOUT);
                    // 关闭channel
                    ctx.channel().close();
                }
            }
        });
        // 添加拆包粘包解码器
        // 参数1：一个数据包最大的长度  参数2：长度字段偏移量  参数3：长度字段占几个字节  参数4：长度的调整值  参数5：需要跳过几个字节数
        nioSocketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 0));
        nioSocketChannel.pipeline().addLast(new RpcDecoder(RpcRequest.class, jsonSerializer));
        nioSocketChannel.pipeline().addLast(new RpcEncoder(RpcResponse.class, jsonSerializer));
        nioSocketChannel.pipeline().addLast(new RpcServerHandler(applicationName, serviceMap, threadPoolExecutor));
    }
}
