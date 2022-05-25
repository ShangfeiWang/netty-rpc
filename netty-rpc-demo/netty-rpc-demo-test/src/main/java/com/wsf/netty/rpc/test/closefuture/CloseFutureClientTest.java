package com.wsf.netty.rpc.test.closefuture;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
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
public class CloseFutureClientTest {

    public static void main(String[] args) {

        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            Channel channel = bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(0, 0, 5, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    if (evt instanceof IdleStateEvent) {
                                        IdleStateEvent event = (IdleStateEvent) evt;
                                        if (event.state() == IdleState.WRITER_IDLE) {
                                            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
                                            buffer.writeBytes("heart".getBytes(StandardCharsets.UTF_8));
                                            ctx.channel().writeAndFlush(buffer);
                                        }
                                    }
                                    super.userEventTriggered(ctx, evt);
                                }
                            });
                        }
                    }).connect(new InetSocketAddress("127.0.0.1", 8080)).sync().channel();
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(16);
            buffer.writeBytes("hello".getBytes(StandardCharsets.UTF_8));
            // 写出的数据一定要是ByteBuf  否则Server会接收不到
            channel.writeAndFlush(buffer);
            TimeUnit.SECONDS.sleep(1000);
            ChannelFuture channelFuture = channel.closeFuture();
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    log.info("处理关闭后的操作");
                }
            });
        } catch (Exception e) {
            log.error("message:{}", e.getMessage());
        } finally {
            group.shutdownGracefully();
        }

    }
}
