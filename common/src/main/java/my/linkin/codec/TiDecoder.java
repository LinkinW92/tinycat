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
        super(20 * 1024 * 1024, 0, 4, 0, 4);
        this.pool = pool;
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            ByteBuffer byteBuffer = in.nioBuffer();
            TiCommand tc = TiCommand.decode(byteBuffer);
            // we should skip all bytes here, otherwise an exception will occur
            in.skipBytes(in.readableBytes());
            return tc;
        } catch (Exception e) {
            log.warn("Failed to decode the message,ex: {}", e);
            this.pool.close(ctx.channel().localAddress());
        } finally {
            if (null != frame) {
                frame.release();
            }
        }
        return null;
    }
}
