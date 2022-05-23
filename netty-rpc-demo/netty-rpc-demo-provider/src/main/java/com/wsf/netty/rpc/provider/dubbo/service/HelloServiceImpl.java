package com.wsf.netty.rpc.provider.dubbo.service;

import com.wsf.netty.rpc.common.annotation.NettyRpcService;
import com.wsf.netty.rpc.common.service.HelloService;
import org.springframework.stereotype.Service;

/**
 * @author wsf
 * @since 20220526
 */
@Service
@NettyRpcService(value = HelloService.class, version = "1.0.0")
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "response: hello " + name;
    }
}
