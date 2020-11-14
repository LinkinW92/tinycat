package my.linkin.server.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import my.linkin.entity.Entity;
import my.linkin.entity.Heartbeat;
import my.linkin.entity.TiCommand;
import my.linkin.server.ServerConfig;
import my.linkin.server.TinyServer;
import my.linkin.util.Helper;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chunhui.wu
 */
@Slf4j
public class HeartbeatProcessor extends CommandProcessor {
    public HeartbeatProcessor(TinyServer server) {
        super(server);
    }

    @Override
    public void process(ChannelHandlerContext ctx, TiCommand cmd) {
        Heartbeat beat = Entity.decode(Heartbeat.class, cmd.getBody());
        processHeartbeat(ctx, beat);
    }

    /**
     * process for heartbeat command, just send a "pong" to the client
     */
    private void processHeartbeat(ChannelHandlerContext ctx, Heartbeat beat) {
        final SocketAddress clientAddr = ctx.channel().localAddress();
        if (beat != null) {
            log.info("{}", beat.getMessage());
        } else {
            log.info("Server receive a heartbeat from the client:{}", Helper.identifier(clientAddr));
        }
        TiCommand cmd = TiCommand.heartbeat();
        Heartbeat pong = Heartbeat.pong();
        cmd.setBody(pong.encode());
        pong(ctx, cmd, clientAddr, new AtomicInteger(ServerConfig.maxRetryTimes));
    }

    /**
     * send pong to client
     */
    private void pong(final ChannelHandlerContext ctx, final TiCommand cmd, final SocketAddress clientAddr, final AtomicInteger retryTimes) {
        if (retryTimes.get() == 0) {
            log.warn("Server failed to send pong to client, now we have to remove the channel the client bind");
            super.server.getPool().close(clientAddr);
            return;
        }
        ctx.writeAndFlush(cmd).addListener((Future<? super Void> future) -> {
            if (!future.isSuccess()) {
                Thread.sleep((ServerConfig.maxRetryTimes - retryTimes.get()) * 1000);
                retryTimes.getAndSet(retryTimes.decrementAndGet());
                super.server.getPublicExecutor().execute(() -> pong(ctx, cmd, clientAddr, retryTimes));
            }
        });
    }
}
