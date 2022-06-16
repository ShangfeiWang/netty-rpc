package com.wsf.netty.redis.client.service.impl;

import com.wsf.netty.redis.client.service.AbstractRedisClient;
import com.wsf.netty.redis.client.service.enums.ClientTypeEnum;
import com.wsf.netty.redis.client.service.enums.ExpireMode;
import com.wsf.netty.redis.client.service.enums.XMode;
import com.wsf.netty.redis.client.utils.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author wsf
 * @since 20220623
 */
public class RedisStringClientImpl extends AbstractRedisClient<String> {

    public RedisStringClientImpl(ClientTypeEnum clientTypeEnum) {
        super(clientTypeEnum);
    }

    @Override
    public boolean set(String key, String value) {
        // set key value
        String cmd = StringUtils.join(Arrays.asList("set", key, value), Constants.SPACE_CHARACTER);
        try {
            String invoke = (String) invoke(cmd);
            return StringUtils.equals(invoke, "OK");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean setNx(String key, String value) {
        // setnx key value
        String cmd = StringUtils.join(Arrays.asList("setnx", key, value), Constants.SPACE_CHARACTER);
        try {
            Long result = (Long) invoke(cmd);
            return result == 1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean setWithExpireTime(String key, String value, long seconds) {
        //setex key time value
        String cmd = StringUtils.join(Arrays.asList("setex", key, seconds, value), Constants.SPACE_CHARACTER);
        try {
            String result = (String) invoke(cmd);
            return StringUtils.equals(result, "OK");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean set(String key, String value, ExpireMode expireMode, long expireTime, XMode x) {
        String cmd = StringUtils.join(Arrays.asList("set", key, value, expireMode.getType(), expireTime, x.getType()), Constants.SPACE_CHARACTER);
        try {
            String result = (String) invoke(cmd);
            return StringUtils.equals(result, "OK");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String get(String key) {
        // get key
        String cmd = StringUtils.join(Arrays.asList("get", key), Constants.SPACE_CHARACTER);
        try {
            return (String) invoke(cmd);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> keysSearch(String key) {
        String cmd = StringUtils.join(Arrays.asList("keys", "*" + key + "*"), Constants.SPACE_CHARACTER);
        try {
            return (List<String>) invoke(cmd);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
