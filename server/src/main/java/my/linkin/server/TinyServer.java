package my.linkin.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import my.linkin.AutoChannelRemovalHandler;
import my.linkin.channel.ChannelPool;
import my.linkin.cluster.ClusterConfig;
import my.linkin.cluster.Handshake;
import my.linkin.cluster.TiClusterNode;
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
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;
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
     * public executor for some asynchronous tasks
     */
    private ExecutorService publicExecutor;

    /**
     * the cluster node info about current server if the cluster mode is enable
     */
    private TiClusterNode node;

    /**
     * the scheduled executor for handshake
     */
    private ScheduledExecutorService handshakeExecutor;

    /**
     * the netty bootstrap
     */
    private ServerBootstrap bootstrap;

    public TinyServer(int port, boolean clusterModeEnable) {
        this.port = port;
        this.bootstrap = new ServerBootstrap();
        this.pool = new ChannelPool(bootstrap, 5, "TinyServer");
        this.publicExecutor = new ThreadPoolExecutor(ServerConfig.publicExecutorCoreSize,
                ServerConfig.publicExecutorCoreSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(ServerConfig.publicWorkerQueueSize),
                new TiThreadFactory("TinyServer"),
                new ThreadPoolExecutor.CallerRunsPolicy());
        if (clusterModeEnable) {
            this.node = new TiClusterNode(TiClusterNode.Role.LOOKING, Helper.fakeAddr(port));
            this.handshakeExecutor = new ScheduledThreadPoolExecutor(ClusterConfig.CLUSTER_SIZE, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "handshake");
                }
            });
        }
        start(clusterModeEnable);
    }

    private void start(boolean clusterModeEnable) {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();
        try {
            bootstrap.group(boss, work)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
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
        } catch (Exception e) {
            log.warn("Failed to bootstrap tiny server, ex:{}", e);
            throw new TiException("Bootstrap tiny server failed");
        }

        if (clusterModeEnable) {
            loadClusterMap();
            this.handshakeExecutor.scheduleAtFixedRate(new HandshakeTask(), 10, 30, TimeUnit.SECONDS);
        }
    }

    /**
     * in the bootstrap, we need to full fill the {@link TiClusterNode#getClusterMap()}
     */
    private void loadClusterMap() {
        InetSocketAddress local = Helper.fakeAddr(port);
        for (InetSocketAddress peer : ClusterConfig.CLUSTER_NODES) {
            if (local.equals(peer)) {
                continue;
            }
            SocketChannel sc = Helper.connect(peer);
            // TODO try again in order to avoid the exception caused by Network jitter
            if (sc == null) {
                continue;
            }
            try {
                long beginTime = System.currentTimeMillis();
                TiCommand cmd = TiCommand.handshake().of(Handshake.initial(new TiClusterNode(TiClusterNode.Role.LOOKING, peer)));
                ByteBuffer writeBuffer = cmd.encode();
                while (writeBuffer.hasRemaining()) {
                    int length = sc.write(writeBuffer);
                    if (length > 0) {
                        if (System.currentTimeMillis() - beginTime > ClusterConfig.HANDSHAKE_TIMEOUT) {
                            throw new TiException("Timeout for initial handshake");
                        }
                    } else {
                        throw new TiException("Fatal error happens in initial handshake");
                    }
                    Thread.sleep(1);
                    // the first byte is opType, the second byte is the length of the cmd
                    int size = writeBuffer.get(1);
                    final ByteBuffer readBuffer = ByteBuffer.allocate(size);
                    while (readBuffer.hasRemaining()) {
                        length = sc.read(readBuffer);
                        if (length > 0) {
                            if (System.currentTimeMillis() - beginTime > ClusterConfig.HANDSHAKE_TIMEOUT) {
                                throw new TiException("Read timeout for initial handshake");
                            }
                        } else {
                            throw new TiException("Read timeout for initial handshake");
                        }
                        Thread.sleep(1);
                    }
                    readBuffer.flip();
                    TiCommand response = TiCommand.decode(readBuffer);
                    Handshake shake = Entity.decode(Handshake.class, response.getBody());
                    this.node.map(shake.getNode());
                }
            } catch (Exception e) {
                log.warn("Failed to initial the handshake for node: {}, ex:{}", Helper.identifier(peer), e);
            }
        }
    }

    private ChannelFuture bindWithRetrying(final ServerBootstrap bootstrap, final int port) {
        final AtomicInteger limit = new AtomicInteger(0);
        final SocketAddress bindAddr = new InetSocketAddress("localhost", port);
        return bootstrap.bind(bindAddr).addListener((Future<? super Void> future) -> {
            if (future.isSuccess()) {
                log.info("Server bind success, start up on addr, {}", Helper.identifier(bindAddr));
            } else {
                if (limit.get() < maxRetryLimit) {
                    log.info("Server bind failed, try again for next port...");
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
            case HANDSHAKE:
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
        pong(ctx, cmd, clientAddr, new AtomicInteger(ServerConfig.maxRetryTimes));
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
                Thread.sleep((ServerConfig.maxRetryTimes - retryTimes.get()) * 1000);
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

    class HandshakeTask implements Runnable {

        @Override
        public void run() {

        }
    }
}
