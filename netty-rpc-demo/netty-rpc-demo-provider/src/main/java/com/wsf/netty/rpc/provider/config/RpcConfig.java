package com.wsf.netty.rpc.provider.config;

import com.wsf.netty.rpc.provider.server.impl.RpcServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wsf
 * @since 20220526
 */
@Configuration
public class RpcConfig {

    private String application = "";

    private String address = "";

    @Bean
    public RpcServer rpcServer() {
        return new RpcServer(application, address);
    }
}
