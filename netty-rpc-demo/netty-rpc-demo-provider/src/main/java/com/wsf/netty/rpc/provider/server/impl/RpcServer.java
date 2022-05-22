package com.wsf.netty.rpc.provider.server.impl;

import com.wsf.netty.rpc.common.annotation.NettyRpcService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @author wsf
 * @since 20220526
 */
public class RpcServer extends NettyServer implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

        Map<String, Object> beansWithAnnotationMap = applicationContext.getBeansWithAnnotation(NettyRpcService.class);
        if (MapUtils.isNotEmpty(beansWithAnnotationMap)) {
            // 注册服务
            for (Object serviceBean : beansWithAnnotationMap.values()) {
                NettyRpcService annotation = serviceBean.getClass().getAnnotation(NettyRpcService.class);
                String interfaceName = annotation.value().getName();
                String version = annotation.version();
                addService(applicationName, interfaceName, version, serviceBean);
            }
        }

    }

    public RpcServer(String application, String serverAddress, String registryAddress) {
        super(application, serverAddress, registryAddress);

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 启动服务器
        start();
    }
}
