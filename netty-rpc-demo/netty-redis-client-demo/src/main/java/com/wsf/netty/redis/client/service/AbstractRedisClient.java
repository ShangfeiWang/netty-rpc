package com.wsf.netty.redis.client.service;

import com.wsf.netty.redis.client.pool.RedisConnection;
import com.wsf.netty.redis.client.pool.RedisConnectionPool;
import com.wsf.netty.redis.client.service.enums.ClientTypeEnum;
import com.wsf.netty.redis.client.utils.RedisResponseHandleUtils;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.redis.ArrayRedisMessage;
import io.netty.handler.codec.redis.FullBulkStringRedisMessage;
import io.netty.handler.codec.redis.RedisMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wsf
 * @since 20220623
 */
public abstract class AbstractRedisClient<T> implements RedisClient<T> {

    private RedisConnectionPool redisConnectionPool;

    public AbstractRedisClient(ClientTypeEnum clientTypeEnum) {
        redisConnectionPool = new RedisConnectionPool(clientTypeEnum);
    }

    protected Object invoke(String command) throws InterruptedException {
        RedisConnection connection = null;
        try {
            connection = redisConnectionPool.borrowRedisConnection();
            if (connection.isActive()) {
                // 锁定连接
                connection.lock();
                // 将命令发送出去
                connection.writeAndFlush(buildRedisRequestMessage(command)).sync();
                RedisMessage response = connection.getResponse(5);
                return RedisResponseHandleUtils.resolveAggregatedRedisResponse(response);
            } else {
                throw new RuntimeException("can not get connection form connection pool");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.unlock();
            }
            if (redisConnectionPool.checkChannel(connection)) {
                redisConnectionPool.returnConnection(connection);
            }
        }
        return null;
    }

    private ArrayRedisMessage buildRedisRequestMessage(String command) {
        // 根据空格进行分割命令
        String[] split = command.split("\\s+");
        List<RedisMessage> redisMessageList = new ArrayList<>();
        for (String cmd : split) {
            redisMessageList.add(new FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT, cmd)));
        }
        return new ArrayRedisMessage(redisMessageList);
    }

}
