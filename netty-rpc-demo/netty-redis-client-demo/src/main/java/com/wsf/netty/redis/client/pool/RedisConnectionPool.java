package com.wsf.netty.redis.client.pool;

import com.wsf.netty.redis.client.config.RedisConfig;
import com.wsf.netty.redis.client.handler.RedisResponseHandler;
import com.wsf.netty.redis.client.service.enums.ClientTypeEnum;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.redis.RedisArrayAggregator;
import io.netty.handler.codec.redis.RedisBulkStringAggregator;
import io.netty.handler.codec.redis.RedisDecoder;
import io.netty.handler.codec.redis.RedisEncoder;
import io.netty.handler.codec.redis.RedisMessage;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * @author wsf
 * @since 20220623
 */
@Slf4j
public class RedisConnectionPool {

    private BlockingQueue<RedisConnection> connectionPool;

    public RedisConnectionPool(ClientTypeEnum clientTypeEnum) {
        connectionPool = new LinkedBlockingQueue<>(RedisConfig.connectionCount);
        initConnectionPool(clientTypeEnum);
    }

    private void initConnectionPool(ClientTypeEnum clientTypeEnum) {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);
        try {
            // 线程池中连接的数量
            int count = RedisConfig.connectionCount;
            int maxConnectionCount = RedisConfig.connectionCount * 2;
            while (connectionPool.size() < count && maxConnectionCount-- > 0) {
                final SynchronousQueue<RedisMessage> synchronousQueue = new SynchronousQueue<>(true);
                ChannelFuture channelFuture = bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast("redisEncoder", new RedisEncoder());
                        ch.pipeline().addLast("redisDecoder", new RedisDecoder());
                        ch.pipeline().addLast("redisBulkStringAggregator", new RedisBulkStringAggregator());
                        ch.pipeline().addLast("redisArrayAggregator", new RedisArrayAggregator());
                        ch.pipeline().addLast("redisResponseHandler", new RedisResponseHandler(synchronousQueue));
                    }
                }).connect(new InetSocketAddress(RedisConfig.host, RedisConfig.port)).sync();

                Channel channel = channelFuture.channel();
                if (channel.isActive()) {
                    String connectionName = "connect-" + connectionPool.size();
                    connectionPool.add(new RedisConnection(connectionName, (NioSocketChannel) channel, synchronousQueue));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从连接池中取出连接
     *
     * @return redis连接
     */
    public RedisConnection borrowRedisConnection() {
        try {
            RedisConnection connection = connectionPool.take();
            log.info("borrowRedisConnection : {}", connection.getConnectionName());
            return connection;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 归还连接
     */
    public void returnConnection(RedisConnection redisConnection) {
        boolean flag = connectionPool.offer(redisConnection);
        if (!flag) {
            // 再试一次
            flag = connectionPool.offer(redisConnection);
        }
        if (!flag) {
            redisConnection.disConnect();
            redisConnection.close();
        }
    }

    public boolean checkChannel(RedisConnection redisConnection) {
        return redisConnection != null && redisConnection.isActive();
    }
}
