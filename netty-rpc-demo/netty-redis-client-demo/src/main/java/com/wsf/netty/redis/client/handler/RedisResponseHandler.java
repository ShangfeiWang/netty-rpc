package com.wsf.netty.redis.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.redis.RedisMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.SynchronousQueue;

/**
 * @author wsf
 * @since 20220623
 */
@Slf4j
public class RedisResponseHandler<T> extends ChannelInboundHandlerAdapter {

    /**
     * 该channel对应的同步任务队列
     */
    private SynchronousQueue<RedisMessage> synchronousQueue;

    public RedisResponseHandler(SynchronousQueue<RedisMessage> synchronousQueue) {
        this.synchronousQueue = synchronousQueue;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("receive redis server message :{}", msg);
        synchronousQueue.put((RedisMessage) msg);
    }

}
