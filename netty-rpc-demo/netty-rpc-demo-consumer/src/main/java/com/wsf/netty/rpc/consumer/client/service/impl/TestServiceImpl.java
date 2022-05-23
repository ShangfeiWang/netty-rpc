package com.wsf.netty.rpc.consumer.client.service.impl;

import com.wsf.netty.rpc.common.annotation.NettyRpcClient;
import com.wsf.netty.rpc.common.service.HelloService;
import com.wsf.netty.rpc.consumer.client.service.TestService;
import org.springframework.stereotype.Service;

/**
 * @author wsf
 * @since 20220526
 */
@Service
public class TestServiceImpl implements TestService {

    @NettyRpcClient(version = "1.0.0")
    private HelloService helloService;

    public String hello() {
        return helloService.hello("zhangsan");
    }
}
