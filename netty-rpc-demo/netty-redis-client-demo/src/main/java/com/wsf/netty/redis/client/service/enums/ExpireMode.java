package com.wsf.netty.redis.client.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wsf
 * @since 20220623
 */
@Getter
@AllArgsConstructor
public enum ExpireMode {

    /**
     * 过期时间是毫秒
     */
    PX("PX"),
    /**
     * 过期时间是秒
     */
    EX("EX");

    /**
     * 过期时间类型
     */
    private final String type;
}
