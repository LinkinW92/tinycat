package my.linkin.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import my.linkin.channel.ChannelPool;
import my.linkin.entity.Heartbeat;
import my.linkin.entity.OpType;
import my.linkin.entity.TiCommand;
import my.linkin.ex.TiException;
import my.linkin.util.Helper;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chunhui.wu
 */
@Slf4j
public class TinyServer {

    private int port;
    /**
     * server bind失败时，最大的重试次数
     */
    private int maxRetryLimit = 5;

    /**
     * the channel pool for the server
     */
    private ChannelPool pool;

    /**
     * server config
     */
    private static ServerConfig config = new ServerConfig();

    /**
     * public executor for some asynchronous tasks
     */
    private ExecutorService publicExecutor = Executors.newFixedThreadPool(config.getPublicSize());

    /**
     * the netty bootstrap
     */
    private ServerBootstrap bootstrap;

    public TinyServer(int port) {
        this.port = port;
        bootstrap = new ServerBootstrap();
        pool = new ChannelPool(bootstrap, 5, "TinyServer");
    }

    public void start() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();
        bootstrap.group(boss, work)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .attr(AttributeKey.newInstance("serverName"), "Tiny Server")
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .channel(NioServerSocketChannel.class)
                .childHandler(new HttpServerInitializer());

        ChannelFuture f = bindWithRetrying(bootstrap, port).sync();
        f.channel().closeFuture().sync();
    }

    private ChannelFuture bindWithRetrying(final ServerBootstrap bootstrap, final int port) {
        final AtomicInteger limit = new AtomicInteger(0);
        return bootstrap.bind(new InetSocketAddress(port)).addListener((Future<? super Void> future) -> {
            if (future.isSuccess()) {
                log.info("server bind success, start up on port:{}", port);
            } else {
                if (limit.get() < maxRetryLimit) {
                    log.info("server bind failed, try again for next port...");
                    bindWithRetrying(bootstrap, port + 1);
                }
                limit.getAndIncrement();
            }
        });
    }

    /**
     * process the command from the client
     */
    private void processCommand(ChannelHandlerContext ctx, TiCommand cmd) {
        final TiCommand tc = cmd;
        final OpType opType = tc.getHeader().getOpType();
        switch (opType) {
            case HEART_BEAT:
                Heartbeat beat = null;
                if (tc.getHeader().getLength() > 0) {
                    beat = (Heartbeat) Heartbeat.decode(Heartbeat.class, tc.getBody());
                }
                processHeartbeat(ctx, beat);
                break;
            case REQUEST:
                break;
            case RESPONSE:
                break;
            default:
                throw new TiException("The opType is not supported in current version");
        }
    }

    /**
     * process for heartbeat command, just send a "pong" to the client
     */
    private void processHeartbeat(ChannelHandlerContext ctx, Heartbeat beat) {
        final SocketAddress clientAddr = ctx.channel().localAddress();
        if (beat != null) {
            final long beatTime = beat.getBeatTime();
            log.info("Server receive a heartbeat that generated at timestamp:{} from the client:{}", beatTime, Helper.identifier(clientAddr));
        } else {
            log.info("Server receive a heartbeat from the client:{}", Helper.identifier(clientAddr));
        }
        TiCommand cmd = TiCommand.heartbeat();
        Heartbeat pong = new Heartbeat("pong");
        cmd.setBody(pong.encode());
        pong(cmd, clientAddr, new AtomicInteger(config.getMaxRetryTimes()));
    }

    /**
     * send pong to client
     */
    private void pong(final TiCommand cmd, final SocketAddress clientAddr, final AtomicInteger retryTimes) {
        if (retryTimes.get() == 0) {
            log.warn("Server failed to send pong to client, now we have to remove the channel the client bind");
            pool.close(clientAddr);
            return;
        }

        ChannelFuture cf = pool.open(clientAddr, 5000L);
        cf.channel().writeAndFlush(cmd).addListener((Future<? super Void> future) -> {
            if (!future.isSuccess()) {
                Thread.sleep((config.getMaxRetryTimes() - retryTimes.get()) * 1000);
                retryTimes.getAndSet(retryTimes.decrementAndGet());
                publicExecutor.execute(() -> pong(cmd, clientAddr, retryTimes));
            }
        });
    }

    @ChannelHandler.Sharable
    class ServerHandler extends SimpleChannelInboundHandler<TiCommand> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TiCommand cmd) throws Exception {
            processCommand(ctx, cmd);
        }
    }
}
