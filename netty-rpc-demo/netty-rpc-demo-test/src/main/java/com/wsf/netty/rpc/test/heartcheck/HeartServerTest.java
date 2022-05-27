package com.wsf.netty.rpc.test.heartcheck;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class HeartServerTest {

    private static int lossCount = 0;

    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Channel channel = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler());
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new IdleStateHandler(5, 3, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    if (evt instanceof IdleStateEvent) {
                                        IdleStateEvent event = (IdleStateEvent) evt;
                                        if (event.state() == IdleState.READER_IDLE) {
                                            log.info("已经5秒没有收到client message 当前第{}次没有收到服务端数据", lossCount);
                                            if (lossCount >= 3) {
                                                log.info("超过{}次没有收到客户端数据,自动关闭连接", lossCount);
                                                ctx.channel().close();
                                                return;
                                            }
                                            lossCount++;
                                        } else if (event.state() == IdleState.WRITER_IDLE) {
                                            log.info("向客户端发送 ping 命令");
                                            ctx.channel().writeAndFlush("ping");
                                        } else if (event.state() == IdleState.ALL_IDLE) {
                                            ;
                                        }
                                        {
                                            //log.info("读写事件都空闲.....");
                                        }
                                    } else {
                                        super.userEventTriggered(ctx, evt);
                                    }
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    log.error("发生了异常 message:{}", cause.getMessage());

                                }

                                // 在mac os上测试的  客户端强制断开连接，会触发这个方法，windows平台暂时不清楚
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    log.error("断开连接");
                                    ctx.channel().close();
                                }
                            });
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                    if ("ping".equals(msg)) {
                                        String response = "pong";
                                        log.info("收到client send message:{} ,response client message {}", msg, response);
                                        ctx.channel().writeAndFlush(response);
                                        return;
                                    } else if ("pong".equals(msg)) {
                                        log.info("收到server 主动发送的ping 命令的response");
                                        return;
                                    }
                                    log.info("receive client message:{}", msg);
                                    ctx.channel().writeAndFlush("hello client");
                                }
                            });
                        }
                    })
                    .bind(8080).sync().channel();

            channel.closeFuture().sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    log.info("close server");
                }
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

}
