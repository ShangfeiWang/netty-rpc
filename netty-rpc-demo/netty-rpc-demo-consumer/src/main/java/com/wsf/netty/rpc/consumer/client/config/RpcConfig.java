package com.wsf.netty.rpc.consumer.client.config;

import com.wsf.netty.rpc.consumer.client.RpcClient;
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

    private String zkAddress;

    @Bean
    public RpcClient rpcClient() {
        return new RpcClient(zkAddress);
    }
}
