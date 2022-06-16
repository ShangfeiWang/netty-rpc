package com.wsf.netty.redis.client.pool;

import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.redis.RedisMessage;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wsf
 * @since 20220623
 */
public class RedisConnection {

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
    private SynchronousQueue<RedisMessage> synchronousQueue;

    private Lock lock = new ReentrantLock();

    public RedisConnection(String connectionName, NioSocketChannel channel, SynchronousQueue<RedisMessage> synchronousQueue) {
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

    public ChannelFuture writeAndFlush(Object msg) {
        return nioSocketChannel.writeAndFlush(msg);
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public String getConnectionName() {
        return connectionName;
    }

    public RedisMessage getResponse(long timeout) throws InterruptedException {
        return synchronousQueue.poll(timeout, TimeUnit.SECONDS);
    }
}
