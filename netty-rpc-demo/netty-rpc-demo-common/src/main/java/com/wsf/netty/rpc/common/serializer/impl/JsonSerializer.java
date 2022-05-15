package com.wsf.netty.rpc.common.serializer.impl;

import com.alibaba.fastjson.JSON;
import com.wsf.netty.rpc.common.serializer.Serializer;

import java.nio.charset.StandardCharsets;

/**
 * @author wsf
 * @since 20220526
 */
public class JsonSerializer implements Serializer {

    @Override
    public <T> byte[] serialize(T obj) {
        String s = JSON.toJSONString(obj);
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> tClass) {
        return JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), tClass);
    }
}
