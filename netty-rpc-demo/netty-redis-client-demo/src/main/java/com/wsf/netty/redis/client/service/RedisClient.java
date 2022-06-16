package com.wsf.netty.redis.client.service;

import com.wsf.netty.redis.client.service.enums.ExpireMode;
import com.wsf.netty.redis.client.service.enums.XMode;

import java.util.List;

/**
 * @author laihaohua
 */
public interface RedisClient<T> {

    /**
     * set 命令 没有过期时间
     *
     * @param key key
     * @param v value
     * @return 是否添加成功
     */
    boolean set(T key, T v);

    /**
     * SETNX 命令
     *
     * @param key key
     * @param v value
     * @return 是否添加成功
     */
    boolean setNx(T key, T v);

    /**
     * 带有过期时间的set命令
     *
     * @param key key
     * @param v value
     * @param seconds 过期时间
     * @return 是否保存成功
     */
    boolean setWithExpireTime(T key, T v, long seconds);

    /**
     * set key value [EX seconds] [PX milliseconds] [NX|XX]
     *
     * @param key key
     * @param v value
     * @param expireMode 过期模式   秒/毫秒
     * @param expireTime 过期时间
     * @param x 模式
     * @return 是否设置成功
     */
    boolean set(T key, T v, ExpireMode expireMode, long expireTime, XMode x);

    /**
     * get命令
     *
     * @param key key
     * @return value
     */
    T get(T key);

    /**
     * 通配符匹配
     *
     * @param key key
     * @return value
     */
    List<T> keysSearch(T key);

}
