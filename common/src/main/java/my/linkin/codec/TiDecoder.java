package my.linkin.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import my.linkin.channel.ChannelPool;
import my.linkin.entity.TiCommand;

import java.nio.ByteBuffer;

/**
 * @author chunhui.wu
 */
@Slf4j
public class TiDecoder extends LengthFieldBasedFrameDecoder {
    private ChannelPool pool;

    public TiDecoder(ChannelPool pool) {
        super(16777216, 0, 4, 0, 4);
        this.pool = pool;
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                return null;
            }
            ByteBuffer byteBuffer = frame.nioBuffer();
            return TiCommand.decode(byteBuffer);
        } catch (Exception e) {
            this.pool.close(ctx.channel().localAddress());
        } finally {
            if (null != frame) {
                frame.release();
            }
        }
        return null;
    }
}
