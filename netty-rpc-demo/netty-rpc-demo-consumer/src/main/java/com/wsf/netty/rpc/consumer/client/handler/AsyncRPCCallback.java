package com.wsf.netty.rpc.consumer.client.handler;

/**
 * Created by luxiaoxun on 2016-03-17.
 */
public interface AsyncRPCCallback {

    void success(Object result);

    void fail(Exception e);

}
