package com.wsf.netty.rpc.provider.server.handler;

import com.wsf.netty.rpc.common.codec.Beat;
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
        log.info("已经{}秒没有收到客户端的数据了", Beat.BEAT_TIMEOUT);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            if (stateEvent.state() == IdleState.READER_IDLE) {
                lossCount++;
                if (lossCount > 3) {
                    log.info("超过3次没有收到客户端的数据，关闭当前不活跃的通道");
                    ctx.channel().close();
                }
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
