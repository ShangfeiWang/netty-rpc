package com.wsf.netty.rpc.test.redisClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.redis.ArrayRedisMessage;
import io.netty.handler.codec.redis.ErrorRedisMessage;
import io.netty.handler.codec.redis.FullBulkStringRedisMessage;
import io.netty.handler.codec.redis.IntegerRedisMessage;
import io.netty.handler.codec.redis.RedisArrayAggregator;
import io.netty.handler.codec.redis.RedisBulkStringAggregator;
import io.netty.handler.codec.redis.RedisDecoder;
import io.netty.handler.codec.redis.RedisEncoder;
import io.netty.handler.codec.redis.RedisMessage;
import io.netty.handler.codec.redis.SimpleStringRedisMessage;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wsf
 * @since 20220623
 */
@Slf4j
public class RedisClientTest {

    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            ChannelFuture channelFuture = bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler());
                            ch.pipeline().addLast(new RedisEncoder());
                            ch.pipeline().addLast(new RedisDecoder());
                            ch.pipeline().addLast(new RedisBulkStringAggregator());
                            ch.pipeline().addLast(new RedisArrayAggregator());
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    log.info("连接redis成功");
                                    String helloworld = "hset 2002 200101 98001";
                                    String[] command = helloworld.split("\\s+");
                                    List<RedisMessage> redisMessageList = new ArrayList<>();
                                    for (String cmdString : command) {
                                        redisMessageList.add(new FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(ctx.alloc(), cmdString)));
                                    }
                                    ArrayRedisMessage arrayRedisMessage = new ArrayRedisMessage(redisMessageList);
                                    log.info("arrayRedisMessage:{}", arrayRedisMessage);
                                    ctx.writeAndFlush(arrayRedisMessage);
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    printAggregatedRedisResponse((RedisMessage) msg);
                                }

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    log.info("连接断开......");
                                    ctx.close();
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    cause.printStackTrace();
                                    ctx.close();
                                }
                            });
                        }
                    }).connect(new InetSocketAddress("127.0.0.1", 6379)).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

    }

    // 处理redis返回数据
    private static void printAggregatedRedisResponse(RedisMessage msg) {

        if (msg instanceof SimpleStringRedisMessage) {
            System.out.println(((SimpleStringRedisMessage) msg).content());
        } else if (msg instanceof ErrorRedisMessage) {
            System.out.println(((ErrorRedisMessage) msg).content());
        } else if (msg instanceof IntegerRedisMessage) {
            System.out.println(((IntegerRedisMessage) msg).value());
        } else if (msg instanceof FullBulkStringRedisMessage) {
            System.out.println(getString((FullBulkStringRedisMessage) msg));
        } else if (msg instanceof ArrayRedisMessage) {
            for (RedisMessage child : ((ArrayRedisMessage) msg).children()) {
                printAggregatedRedisResponse(child);
            }
        } else {
            throw new CodecException("unknown message type: " + msg + "\r\n");
        }
    }

    private static String getString(FullBulkStringRedisMessage msg) {
        if (msg.isNull()) {
            return "(null)";
        }
        return msg.content().toString(CharsetUtil.UTF_8);
    }
}
