package com.wsf.netty.rpc.test.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class ServerHeatBeatHandler extends ChannelInboundHandlerAdapter {

    private int lossCount = 1;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.info("已经3秒没有收到客户端的数据了");
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            if (stateEvent.state() == IdleState.READER_IDLE) {
                lossCount++;
                if (lossCount > 3) {
                    log.info("超过3次没有收到客户端的数据，关闭当前不活跃的通道");
                    ctx.channel().close();
                }
            } else if (stateEvent.state() == IdleState.WRITER_IDLE) {
                log.info("超过3次没有写出数据了");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("error:{}", cause.getMessage());
        ctx.close();
    }
}
