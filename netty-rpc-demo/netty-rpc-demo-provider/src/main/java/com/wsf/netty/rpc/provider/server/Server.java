package com.wsf.netty.rpc.provider.server;

/**
 * @author wsf
 * @since 20220526
 */
public abstract class Server {

    /**
     * 服务器启动
     */
    public abstract void start();

    /**
     * 服务器停止
     */
    public abstract void stop();

}
