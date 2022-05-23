package com.wsf.netty.rpc.consumer.client.connect;

import com.alibaba.fastjson.JSON;
import com.wsf.netty.rpc.common.protocol.RpcProtocol;
import com.wsf.netty.rpc.common.protocol.RpcServiceInfo;
import com.wsf.netty.rpc.consumer.client.handler.RpcClientHandler;
import com.wsf.netty.rpc.consumer.client.handler.RpcClientInitializer;
import com.wsf.netty.rpc.consumer.client.route.RpcLoadBalanceRandom;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class ConnectionManager {

    public static ConnectionManager connectionManager;

    private CopyOnWriteArraySet<RpcProtocol> rpcProtocolSet = new CopyOnWriteArraySet<>();

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 8, 600L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000));

    private NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    private Map<RpcProtocol, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();

    private ReentrantLock lock = new ReentrantLock();

    private Condition connected = lock.newCondition();

    private volatile boolean isRunning = true;

    private long waitTimeout = 5000;

    private RpcLoadBalanceRandom random = new RpcLoadBalanceRandom();

    private static Object object = new Object();

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        if (connectionManager == null) {
            synchronized (object) {
                if (connectionManager == null) {
                    connectionManager = new ConnectionManager();
                    return connectionManager;
                }
                return connectionManager;
            }
        }
        return connectionManager;
    }

    public void updateConnectedServer(RpcProtocol protocol, PathChildrenCacheEvent.Type type) {
        if (protocol == null) {
            return;
        }
        if (type == PathChildrenCacheEvent.Type.CHILD_ADDED && !rpcProtocolSet.contains(protocol)) {
            connectServerNode(protocol);
        } else if (type == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
            removeAndCloseHandler(protocol);
            connectServerNode(protocol);
        } else if (type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
            removeAndCloseHandler(protocol);
        } else {
            throw new IllegalArgumentException("Unknow type:" + type);
        }

    }

    public void updateConnectedServer(List<RpcProtocol> serviceDateList) {
        if (CollectionUtils.isNotEmpty(serviceDateList)) {
            HashSet<RpcProtocol> serviceSet = new HashSet<>(serviceDateList);
            for (RpcProtocol protocol : serviceSet) {
                if (!rpcProtocolSet.contains(protocol)) {
                    // 不包含 添加新服务
                    connectServerNode(protocol);
                }
            }

            for (RpcProtocol protocol : rpcProtocolSet) {
                if (!serviceSet.contains(protocol)) {
                    // 移除不存在的服务
                    log.info("remote invalid service :{}", JSON.toJSONString(protocol));
                    removeAndCloseHandler(protocol);
                }
            }

        } else {
            // 注册的服务列表为空
            log.warn("没有注册服务！！serviceDateList:{}", serviceDateList);
            for (RpcProtocol protocol : rpcProtocolSet) {
                removeAndCloseHandler(protocol);
            }
        }
    }

    private void removeAndCloseHandler(RpcProtocol protocol) {
        RpcClientHandler rpcClientHandler = connectedServerNodes.get(protocol);
        if (rpcClientHandler != null) {
            rpcClientHandler.close();
        }
        // 从注册的缓存Map中移除
        connectedServerNodes.remove(protocol);
        rpcProtocolSet.remove(protocol);
    }

    private void connectServerNode(RpcProtocol protocol) {
        List<RpcServiceInfo> serviceInfoList = protocol.getServiceInfoList();
        if (CollectionUtils.isEmpty(serviceInfoList)) {
            return;
        }
        rpcProtocolSet.add(protocol);
        log.info("New service node, host: {}, port: {}", protocol.getHost(), protocol.getPort());
        serviceInfoList.forEach(x -> log.info("New service info,application:{}, name: {}, version: {}", x.getApplicationName(), x.getInterfaceName(), x.getVersion()));
        InetSocketAddress inetSocketAddress = new InetSocketAddress(protocol.getHost(), protocol.getPort());
        threadPoolExecutor.execute(() -> {
            Bootstrap bootstrap = new Bootstrap();
            ChannelFuture channelFuture = bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcClientInitializer())
                    .connect(inetSocketAddress);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        log.info("success connect to remote server inetSocketAddress:{}", inetSocketAddress);
                        RpcClientHandler rpcClientHandler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                        connectedServerNodes.put(protocol, rpcClientHandler);
                        rpcClientHandler.setRpcProtocol(protocol);
                        signalAvailableHandler();
                    } else {
                        log.info("fail connect to remote server inetSocketAddress:{}", inetSocketAddress);
                    }
                }
            });
        });
    }

    private void signalAvailableHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void removeHandler(RpcProtocol rpcProtocol) {
        rpcProtocolSet.remove(rpcProtocol);
        connectedServerNodes.remove(rpcProtocol);
        log.info("Remove one connection, host: {}, port: {}", rpcProtocol.getHost(), rpcProtocol.getPort());
    }

    public void stop() {
        isRunning = false;
        for (RpcProtocol protocol : rpcProtocolSet) {
            removeAndCloseHandler(protocol);
        }
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }

    public RpcClientHandler chooseHandler(String serviceKey) throws Exception {
        int size = connectedServerNodes.values().size();
        while (isRunning && size <= 0) {
            try {
                // 当没有服务要注册的时候就在这里等待
                waitForHandler();
                size = connectedServerNodes.values().size();
            } catch (Exception e) {
                log.error("Waiting for available service is interrupted!", e);
            }
        }

        RpcProtocol route = random.route(serviceKey, connectedServerNodes);
        RpcClientHandler rpcClientHandler = connectedServerNodes.get(route);
        if (rpcClientHandler != null) {
            return rpcClientHandler;
        }
        throw new Exception("Can not get available connection");
    }

    private boolean waitForHandler() throws InterruptedException {
        lock.lock();
        try {
            log.info("waiting for available service");
            return connected.await(this.waitTimeout, TimeUnit.SECONDS);
        } finally {
            lock.unlock();
        }
    }
}
