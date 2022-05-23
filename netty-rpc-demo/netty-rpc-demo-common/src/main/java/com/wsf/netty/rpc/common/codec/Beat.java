package com.wsf.netty.rpc.common.codec;

/**
 * @author wsf
 * @since 20220526
 */
public class Beat {

    /**
     * 心跳requestID
     */
    public static final String BEAT_ID = "BEAT_PING_PONG";

    public static final int BEAT_INTERVAL = 30;

    public static final int BEAT_TIMEOUT = 3 * BEAT_INTERVAL;

    public static final String BEAT_PING = "ping";
}
