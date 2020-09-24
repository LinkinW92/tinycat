package my.linkin.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import my.linkin.channel.ChannelPool;
import my.linkin.entity.TiCommand;

/**
 * @author chunhui.wu
 */
@Slf4j
public class TiEncoder extends MessageToByteEncoder<TiCommand> {


    private ChannelPool pool;

    public TiEncoder(ChannelPool pool) {
        this.pool = pool;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, TiCommand cmd, ByteBuf out) throws Exception {
        try {
            out.writeBytes(cmd.encode());
        } catch (Exception e) {
            log.error("Something error happens when encode, now we have to close the channel");
            pool.close(ctx.channel().localAddress());
        }
    }
}
