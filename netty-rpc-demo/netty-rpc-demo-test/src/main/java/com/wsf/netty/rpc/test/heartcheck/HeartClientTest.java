package com.wsf.netty.rpc.test.heartcheck;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class HeartClientTest {

    private static int lossCount = 0;

    public static void main(String[] args) {

        NioEventLoopGroup group = new NioEventLoopGroup();
        try {

            ChannelFuture channelFuture = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            //ch.pipeline().addLast(new LoggingHandler());
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new IdleStateHandler(5, 3, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    if (evt instanceof IdleStateEvent) {
                                        IdleStateEvent event = ((IdleStateEvent) evt);
                                        if (event.state() == IdleState.READER_IDLE) {
                                            log.info("已经5秒没有收到server message 当前第{}次没有收到服务端数据", lossCount);
                                            if (lossCount >= 3) {
                                                log.info("超过{}次没有收到服务端数据,自动切换服务端IP 重新连接其他服务器", lossCount);
                                                ctx.channel().close();
                                                return;
                                            }
                                            lossCount++;
                                        } else if (event.state() == IdleState.WRITER_IDLE) {
                                            log.info("向服务器端发送 ping 命令");
                                            ctx.channel().writeAndFlush("ping");
                                        }
                                    } else {
                                        super.userEventTriggered(ctx, evt);
                                    }
                                }
                            });
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                    if ("ping".equals(msg)) {
                                        String response = "pong";
                                        log.info("收到server message:{} ,response server message {}", msg, response);
                                        ctx.channel().writeAndFlush(response);
                                        return;
                                    } else if ("pong".equals(msg)) {
                                        // 收到客户端主动发送的ping 命令
                                        log.info("收到client 主动发送的ping 命令的response");
                                        return;
                                    }
                                    log.info("receive server message:{}", msg);
                                }
                            });
                        }
                    })
                    .connect(new InetSocketAddress("localhost", 8080)).sync();
            Channel channel = channelFuture.channel();
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
            buffer.writeBytes("hello server".getBytes(StandardCharsets.UTF_8));
            channel.writeAndFlush(buffer);
            channel.closeFuture().sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    log.info("client close .....");
                    System.out.println(future);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

    }

}
