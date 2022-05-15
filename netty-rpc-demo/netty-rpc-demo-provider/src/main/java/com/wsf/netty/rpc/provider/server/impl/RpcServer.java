package com.wsf.netty.rpc.provider.server.impl;

import com.wsf.netty.rpc.common.annotation.NettyRpcService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @author wsf
 * @since 20220526
 */
public class RpcServer extends NettyServer implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public RpcServer(String application, String address) {
        super(address);
        Map<String, Object> beansWithAnnotationMap = applicationContext.getBeansWithAnnotation(NettyRpcService.class);
        if (MapUtils.isNotEmpty(beansWithAnnotationMap)) {
            // 注册服务
            for (Object serviceBean : beansWithAnnotationMap.values()) {
                NettyRpcService annotation = serviceBean.getClass().getAnnotation(NettyRpcService.class);
                String interfaceName = annotation.clazz().getName();
                String version = annotation.version();
                addService(application, interfaceName, version, serviceBean);
            }
        }
        // 启动服务器
        start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
