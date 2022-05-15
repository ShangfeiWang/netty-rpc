package com.wsf.netty.rpc.common.serializer;

/**
 * @author wsf
 * @since 20220526
 */
public interface Serializer {

    /**
     * 序列化对象
     *
     * @param obj 对象数据
     * @param <T> 类型
     * @return 字节数据
     */
    <T> byte[] serialize(T obj);

    /**
     * 反序列化对象
     *
     * @param bytes 字节数组
     * @param tClass 要反序列化成什么对象
     * @param <T> 泛型
     * @return 解码后的数据
     */
    <T> T deSerialize(byte[] bytes, Class<T> tClass);
}
