package com.wsf.netty.redis.client.utils;

import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.redis.ArrayRedisMessage;
import io.netty.handler.codec.redis.ErrorRedisMessage;
import io.netty.handler.codec.redis.FullBulkStringRedisMessage;
import io.netty.handler.codec.redis.IntegerRedisMessage;
import io.netty.handler.codec.redis.RedisMessage;
import io.netty.handler.codec.redis.SimpleStringRedisMessage;
import io.netty.util.CharsetUtil;

/**
 * @author wsf
 * @since 20220623
 */
public class RedisResponseHandleUtils {

    // 处理redis返回数据
    public static Object resolveAggregatedRedisResponse(RedisMessage msg) {
        if (msg instanceof SimpleStringRedisMessage) {
            return ((SimpleStringRedisMessage) msg).content();
        } else if (msg instanceof ErrorRedisMessage) {
            return ((ErrorRedisMessage) msg).content();
        } else if (msg instanceof IntegerRedisMessage) {
            return ((IntegerRedisMessage) msg).value();
        } else if (msg instanceof FullBulkStringRedisMessage) {
            return getString((FullBulkStringRedisMessage) msg);
        } else if (msg instanceof ArrayRedisMessage) {
            for (RedisMessage child : ((ArrayRedisMessage) msg).children()) {
                resolveAggregatedRedisResponse(child);
            }
        } else {
            throw new CodecException("unknown message type: " + msg + "\r\n");
        }
        return null;
    }

    public static String getString(FullBulkStringRedisMessage msg) {
        if (msg.isNull()) {
            return null;
        }
        return msg.content().toString(CharsetUtil.UTF_8);
    }

}
