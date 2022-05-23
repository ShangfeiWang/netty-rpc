package com.wsf.netty.rpc.consumer.client.proxy;

import com.wsf.netty.rpc.common.codec.RpcRequest;
import com.wsf.netty.rpc.common.utils.ServiceUtil;
import com.wsf.netty.rpc.consumer.client.connect.ConnectionManager;
import com.wsf.netty.rpc.consumer.client.handler.RpcClientHandler;
import com.wsf.netty.rpc.consumer.client.handler.RpcFuture;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class ObjectProxy<T, P> implements InvocationHandler, RpcService<T, P, SerializableFunction<T>> {

    private final String application;

    private final String version;

    private final Class<T> clazz;

    public ObjectProxy(Class<T> clazz, String version, String application) {
        this.version = version;
        this.clazz = clazz;
        this.application = application;
    }

    @Override
    public RpcFuture call(String funcName, Object... args) throws Exception {
        return null;
    }

    @Override
    public RpcFuture call(SerializableFunction<T> tSerializableFunction, Object... args) throws Exception {
        return null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equal".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setParameters(args);
        rpcRequest.setVersion(version);

        if (log.isDebugEnabled()) {
            log.debug(method.getDeclaringClass().getName());
            log.debug(method.getName());
            for (int i = 0; i < method.getParameterTypes().length; ++i) {
                log.debug(method.getParameterTypes()[i].getName());
            }
            for (int i = 0; i < args.length; ++i) {
                log.debug(args[i].toString());
            }
        }

        String serviceKey = ServiceUtil.generateUniqueServiceKey(application, method.getDeclaringClass().getName(), version);
        RpcClientHandler rpcClientHandler = ConnectionManager.getInstance().chooseHandler(serviceKey);
        RpcFuture rpcFuture = rpcClientHandler.sendRpcRequest(rpcRequest);
        // 同步获取结果并返回
        return rpcFuture.get();
    }
}
