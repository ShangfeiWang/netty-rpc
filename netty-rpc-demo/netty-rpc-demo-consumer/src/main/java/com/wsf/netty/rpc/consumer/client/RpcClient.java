package com.wsf.netty.rpc.consumer.client;

import com.wsf.netty.rpc.common.annotation.NettyRpcClient;
import com.wsf.netty.rpc.consumer.client.connect.ConnectionManager;
import com.wsf.netty.rpc.consumer.client.discover.ServiceDiscovery;
import com.wsf.netty.rpc.consumer.client.proxy.ObjectProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class RpcClient implements ApplicationContextAware, DisposableBean {

    private ApplicationContext applicationContext;

    private ServiceDiscovery serviceDiscovery;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            if ("rpcClient".equals(beanDefinitionName)) {
                continue;
            }
            Object bean = applicationContext.getBean(beanDefinitionName);
            Field[] declaredFields = bean.getClass().getDeclaredFields();
            try {
                for (Field declaredField : declaredFields) {
                    NettyRpcClient annotation = declaredField.getAnnotation(NettyRpcClient.class);
                    if (annotation != null) {
                        String version = annotation.version();
                        declaredField.setAccessible(true);
                        declaredField.set(bean, createService(declaredField.getType(), version));

                    }
                }
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
            }
        }
    }

    private <T> T createService(Class<T> interfaceClass, String version) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[] { interfaceClass }, new ObjectProxy<>(interfaceClass, version, "provider"));
    }

    public RpcClient(String address) {
        this.serviceDiscovery = new ServiceDiscovery(address);
    }

    @Override
    public void destroy() throws Exception {
        threadPoolExecutor.shutdown();
        serviceDiscovery.stop();
        ConnectionManager.getInstance().stop();
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.execute(task);
    }

}
