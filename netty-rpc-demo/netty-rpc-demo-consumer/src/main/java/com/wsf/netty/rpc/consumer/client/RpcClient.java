package com.wsf.netty.rpc.consumer.client;

import com.wsf.netty.rpc.consumer.client.discover.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class RpcClient implements ApplicationContextAware, DisposableBean {

    private ApplicationContext applicationContext;

    private ServiceDiscovery serviceDiscovery;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public RpcClient(String address) {
        this.serviceDiscovery = new ServiceDiscovery(address);
    }

    @Override
    public void destroy() throws Exception {

    }

}
