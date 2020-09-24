package my.linkin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import my.linkin.channel.ChannelPool;

/**
 * @author chunhui.wu
 * when a channel is timeout, in other words, the channel is idle, then trrigger this
 * handler to remove the idle channel from the channel pool
 */
@Slf4j
public class AutoChannelRemovalHandler extends ChannelInboundHandlerAdapter {


    private ChannelPool pool;

    public AutoChannelRemovalHandler(ChannelPool pool) {
        this.pool = pool;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) {
                ctx.channel().close();
                log.info("This channel:{} has been timeout for read or write, now we close the channel", ctx.channel().localAddress());
                this.pool.close(ctx.channel().localAddress());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
