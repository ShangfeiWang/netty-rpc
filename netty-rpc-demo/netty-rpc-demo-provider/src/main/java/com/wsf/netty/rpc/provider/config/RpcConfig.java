package com.wsf.netty.rpc.provider.config;

import com.wsf.netty.rpc.provider.server.impl.RpcServer;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wsf
 * @since 20220526
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rpc.netty")
public class RpcConfig {

    private String application;

    private String address;

    private String zkAddress;

    @Bean
    public RpcServer rpcServer() {
        return new RpcServer(application, address, zkAddress);
    }
}
