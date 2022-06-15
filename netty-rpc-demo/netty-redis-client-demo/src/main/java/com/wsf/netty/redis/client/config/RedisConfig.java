package com.wsf.netty.redis.client.config;

import lombok.Data;

/**
 * @author wsf
 * @since 20220623
 */
@Data
public class RedisConfig {

    public static int connectionCount = 10;

    public static int port;

    public static String host;

}
