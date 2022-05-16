package com.wsf.netty.rpc.provider.server.handler;

import com.wsf.netty.rpc.common.codec.Beat;
import com.wsf.netty.rpc.common.codec.RpcRequest;
import com.wsf.netty.rpc.common.codec.RpcResponse;
import com.wsf.netty.rpc.common.utils.ServiceUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.reflect.FastClass;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private Map<String, Object> serviceMap;

    private ThreadPoolExecutor threadPoolExecutor;

    private String applicationName;

    public RpcServerHandler(String applicationName, Map<String, Object> serviceMap, ThreadPoolExecutor threadPoolExecutor) {
        this.serviceMap = serviceMap;
        this.threadPoolExecutor = threadPoolExecutor;
        this.applicationName = applicationName;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        // 过滤心跳包
        if (Beat.BEAT_ID.equals(rpcRequest.getRequestId())) {
            log.info("读取到的是心跳数据，不做处理");
            return;
        }
        threadPoolExecutor.execute(() -> {
            log.info("收到client请求 requestId:{}", rpcRequest.getRequestId());
            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setRequestId(rpcRequest.getRequestId());
            try {
                Object result = handlerRequest(rpcRequest);
                rpcResponse.setResult(result);
                rpcResponse.setSuccess(true);
            } catch (Exception e) {
                rpcResponse.setSuccess(false);
                rpcResponse.setErrorMessage(e.getMessage());
            }
            channelHandlerContext.writeAndFlush(rpcResponse).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    log.info("send response for request requestId:{}", rpcRequest.getRequestId());
                }
            });
        });

    }

    private Object handlerRequest(RpcRequest rpcRequest) throws InvocationTargetException {
        String serviceKey = ServiceUtil.generateUniqueServiceKey(applicationName, rpcRequest.getClassName(), rpcRequest.getVersion());
        Object serviceBean = serviceMap.get(serviceKey);
        if (serviceBean == null) {
            log.error("Can not find service implement with interface name: {} and version: {}", rpcRequest.getClassName(), rpcRequest.getVersion());
            return null;
        }
        Class<?> serviceBeanClass = serviceBean.getClass();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();
        log.info("将要代理的类型：{},方法名称：{}, 参数的类型数组{}, 参数：{}", serviceBeanClass.getName(), methodName, parameterTypes, parameters);
        FastClass fastClass = FastClass.create(serviceBeanClass);
        int methodIndex = fastClass.getIndex(methodName, parameterTypes);
        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Server caught exception :{}", cause.getMessage());
        ctx.channel().close();
    }
}
