package com.wsf.netty.rpc.test.reconnect;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class ReconnectClientTest {

    private static String host;

    private static int port;

    private static Bootstrap bootstrap;

    private static EventLoopGroup group;

    private static int count = 1;

    public static void main(String[] args) throws InterruptedException {
        host = "localhost";
        port = 8080;
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //ch.pipeline().addLast(new LoggingHandler());
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                ctx.writeAndFlush("zhangsan");
                            }
                        });
                    }
                });
        // 连接server
        connect(bootstrap, host, port);

    }

    public static void connect(Bootstrap bootstrap, String host, int port) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(host, port);
        // 添加一个监听
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                // 假如没有连接成功，就进行重连
                if (!future.isSuccess()) {
                    EventLoop eventExecutors = future.channel().eventLoop();
                    eventExecutors.schedule(() -> {
                        log.info("reconnect server.......");
                        try {
                            connect(bootstrap, host, port);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, 3000, TimeUnit.MILLISECONDS);
                } else {
                    // 否则连接成功
                    log.info("connect success.....");
                }
            }
        });
        // 监听关闭通道
        channelFuture.channel().closeFuture().sync();

    }

}
