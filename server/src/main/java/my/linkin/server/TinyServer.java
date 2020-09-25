package my.linkin.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import my.linkin.AutoChannelRemovalHandler;
import my.linkin.channel.ChannelPool;
import my.linkin.codec.TiDecoder;
import my.linkin.codec.TiEncoder;
import my.linkin.entity.Entity;
import my.linkin.entity.Heartbeat;
import my.linkin.entity.OpType;
import my.linkin.entity.TiCommand;
import my.linkin.ex.TiException;
import my.linkin.thread.TiThreadFactory;
import my.linkin.util.Helper;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    private ExecutorService publicExecutor;

    /**
     * the netty bootstrap
     */
    private ServerBootstrap bootstrap;

    public TinyServer(int port) {
        this.port = port;
        this.bootstrap = new ServerBootstrap();
        this.pool = new ChannelPool(bootstrap, 5, "TinyServer");
        this.publicExecutor = new ThreadPoolExecutor(config.getPublicExecutorCoreSize(),
                config.getPublicExecutorCoreSize(),
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(config.getPublicWorkerQueueSize()),
                new TiThreadFactory("TinyServer"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void start() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();
        bootstrap.group(boss, work)
//                .handler(new LoggingHandler(LogLevel.DEBUG))
                .attr(AttributeKey.newInstance("serverName"), "Tiny Server")
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.SO_REUSEADDR, true)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(5000, 5000, 0, TimeUnit.MILLISECONDS));
                        // auto remove the channel from the channel pool if it becomes idle
                        pipeline.addLast(new AutoChannelRemovalHandler(pool));
                        pipeline.addLast(new TiDecoder(pool));
                        pipeline.addLast(new TiEncoder(pool));
                        pipeline.addLast(new ServerHandler(pool));
                    }
                });

        ChannelFuture f = bindWithRetrying(bootstrap, port).sync();
        f.channel().closeFuture().sync();
    }

    private ChannelFuture bindWithRetrying(final ServerBootstrap bootstrap, final int port) {
        final AtomicInteger limit = new AtomicInteger(0);
        final SocketAddress bindAddr = new InetSocketAddress("localhost", port);
        return bootstrap.bind(bindAddr).addListener((Future<? super Void> future) -> {
            if (future.isSuccess()) {
                log.info("server bind success, start up on addr, {}", Helper.identifier(bindAddr));
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
                    beat = Entity.decode(Heartbeat.class, tc.getBody());
                }
                processHeartbeat(ctx, beat);
                break;
            case REQUEST:
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
            log.info("{}", beat.getMessage());
        } else {
            log.info("Server receive a heartbeat from the client:{}", Helper.identifier(clientAddr));
        }
        TiCommand cmd = TiCommand.heartbeat();
        Heartbeat pong = Heartbeat.pong();
        cmd.setBody(pong.encode());
        pong(ctx, cmd, clientAddr, new AtomicInteger(config.getMaxRetryTimes()));
    }

    /**
     * send pong to client
     */
    private void pong(final ChannelHandlerContext ctx, final TiCommand cmd, final SocketAddress clientAddr, final AtomicInteger retryTimes) {
        if (retryTimes.get() == 0) {
            log.warn("Server failed to send pong to client, now we have to remove the channel the client bind");
            pool.close(clientAddr);
            return;
        }
        ctx.writeAndFlush(cmd).addListener((Future<? super Void> future) -> {
            if (!future.isSuccess()) {
                Thread.sleep((config.getMaxRetryTimes() - retryTimes.get()) * 1000);
                retryTimes.getAndSet(retryTimes.decrementAndGet());
                publicExecutor.execute(() -> pong(ctx, cmd, clientAddr, retryTimes));
            }
        });
    }

    @ChannelHandler.Sharable
    class ServerHandler extends SimpleChannelInboundHandler<TiCommand> {

        private ChannelPool pool;

        public ServerHandler(ChannelPool pool) {
            this.pool = pool;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TiCommand cmd) throws Exception {
            try {
                processCommand(ctx, cmd);
            } catch (Exception e) {
                log.warn("An error occurs when process command, ex:{}", e);
            }
        }
    }
}
