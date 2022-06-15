package com.wsf.netty.redis.client.pool;

import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wsf
 * @since 20220623
 */
public class RedisConnection<T> {

    /**
     * redis连接的名称
     */
    private String connectionName;

    /**
     * redis连接对应的socketChannel
     */
    private NioSocketChannel nioSocketChannel;

    /**
     * 该连接的同步队列
     */
    private SynchronousQueue<T> synchronousQueue;

    private Lock lock = new ReentrantLock();

    public RedisConnection(String connectionName, NioSocketChannel channel, SynchronousQueue<T> synchronousQueue) {
        this.connectionName = connectionName;
        this.nioSocketChannel = channel;
        this.synchronousQueue = synchronousQueue;
    }

    public boolean isActive() {
        return nioSocketChannel.isActive();
    }

    public void close() {
        nioSocketChannel.close();
    }

    public void disConnect() {
        nioSocketChannel.disconnect();
    }

}
