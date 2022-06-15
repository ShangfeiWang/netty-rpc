package com.wsf.netty.redis.client.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wsf
 * @since 20220623
 */
@Getter
@AllArgsConstructor
public enum XMode {

    /**
     * key存在的时候才创建
     */
    XX("XX"),
    /**
     * key不存在的时候才创建
     */
    NX("EX");

    /**
     * 类型
     */
    private final String type;

}
