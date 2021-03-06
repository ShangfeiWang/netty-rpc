package com.wsf.netty.rpc.provider.server.impl;

import com.wsf.netty.rpc.common.utils.ServiceUtil;
import com.wsf.netty.rpc.common.utils.ThreadPoolUtil;
import com.wsf.netty.rpc.provider.server.Server;
import com.wsf.netty.rpc.provider.server.handler.RpcServiceInitializer;
import com.wsf.netty.rpc.provider.server.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class NettyServer extends Server {

    // 127.0.0.1:12345
    protected String address;

    protected String applicationName;

    private ServiceRegistry serviceRegistry;

    private final Map<String, Object> SERVICE_MAP = new HashMap<>();

    public NettyServer(String applicationName, String address, String zkAddress) {
        this.address = address;
        this.applicationName = applicationName;
        this.serviceRegistry = new ServiceRegistry(zkAddress);
    }

    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup();

    private final NioEventLoopGroup workGroup = new NioEventLoopGroup();

    /**
     * 添加服务
     *
     * @param application 应用名称
     * @param interfaceName 接口名称
     * @param version 版本号
     * @param serviceBean serviceBean
     */
    public void addService(String application, String interfaceName, String version, Object serviceBean) {
        String uniqueKey = ServiceUtil.generateUniqueServiceKey(application, interfaceName, version);
        SERVICE_MAP.put(uniqueKey, serviceBean);
    }

    @Override
    public void start() {
        ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.makeServerThreadPool(NettyServer.class.getSimpleName(), 16, 32);
        String[] split = address.split(":");
        String hostName = split[0];
        int port = Integer.parseInt(split[1]);

        try {
            ChannelFuture channelFuture = new ServerBootstrap()
                    .group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new RpcServiceInitializer(applicationName, SERVICE_MAP, threadPoolExecutor))
                    // todo 两个option的含义
                    //.option(ChannelOption.SO_BACKLOG, 128)
                    //.childOption(ChannelOption.SO_KEEPALIVE, true)
                    .bind(new InetSocketAddress(hostName, port)).sync();
            // 服务注册到zk
            if (serviceRegistry != null) {
                serviceRegistry.registryService(hostName, port, SERVICE_MAP);
            }
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("RpcServer 启动异常", e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}
