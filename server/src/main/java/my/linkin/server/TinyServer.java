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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import my.linkin.AutoChannelRemovalHandler;
import my.linkin.channel.ChannelPool;
import my.linkin.cluster.ClusterConfig;
import my.linkin.cluster.Handshake;
import my.linkin.cluster.TiClusterInfo;
import my.linkin.cluster.TiClusterNode;
import my.linkin.codec.TiDecoder;
import my.linkin.codec.TiEncoder;
import my.linkin.entity.Entity;
import my.linkin.entity.TiCommand;
import my.linkin.ex.TiException;
import my.linkin.server.processor.CommandDispatcher;
import my.linkin.thread.TiThreadFactory;
import my.linkin.util.Helper;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chunhui.wu
 */
@Slf4j
@Data
public class TinyServer {

    private int port;
    /**
     * the max retry times for bootstrap.bind operation
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
     * the scheduled executor for handshake
     */
    private ScheduledExecutorService handshakeExecutor;

    /**
     * the netty bootstrap
     */
    private ServerBootstrap bootstrap;

    /**
     * If cluster mode is enable, we use clusterInfo to maintain the details about tiCluster
     */
    private TiClusterInfo clusterInfo = new TiClusterInfo();

    private CommandDispatcher dispatcher;


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
            clusterInfo.node = new TiClusterNode(TiClusterNode.Role.LOOKING, Helper.fakeAddr(port));
            this.handshakeExecutor = new ScheduledThreadPoolExecutor(ClusterConfig.CLUSTER_SIZE, (Runnable r) -> new Thread(r, "handshake"));
        }

        this.dispatcher = new CommandDispatcher(this);

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
                            // auto remove the client channel from the channel pool if it becomes idle
                            pipeline.addLast(new AutoChannelRemovalHandler(pool));
                            pipeline.addLast(new TiDecoder(pool));
                            pipeline.addLast(new TiEncoder(pool));
                            pipeline.addLast(new ServerHandler());
                        }
                    });

            bindWithRetrying(bootstrap, port).sync();
        } catch (Exception e) {
            log.warn("Failed to bootstrap tiny server, ex:{}", e);
            throw new TiException("Bootstrap tiny server failed");
        }

        this.dispatcher.init();

        if (clusterModeEnable) {
            try {
                Thread.sleep(1000 * 10);
            } catch (Exception e) {

            }
            this.loadClusterMap();
            // TODO check one more time
//            this.handshakeExecutor.scheduleWithFixedDelay(this::loadClusterMap, 5, 0, TimeUnit.SECONDS);
            this.handshakeExecutor.scheduleAtFixedRate(new HandshakeTask(), 1, 15, TimeUnit.SECONDS);
        }
    }

    /**
     * in the bootstrap, we need to full fill the {@link TiClusterInfo#getClusterMap()}
     */
    private void loadClusterMap() {
        InetSocketAddress local = Helper.fakeAddr(port);
        for (InetSocketAddress peer : ClusterConfig.CLUSTER_NODES) {
            if (local.equals(peer)) {
                continue;
            }
            TiCommand cmd = TiCommand.handshake().of(Handshake.initial(this.clusterInfo.node));
            handshakeToPeerNode(peer, cmd);
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
     * handshake to peer node
     */
    private void handshakeToPeerNode(InetSocketAddress peer, TiCommand cmd) {
        String identifier = Helper.identifier(peer);

        SocketChannel sc = this.clusterInfo.getCachedChannel(identifier);
        if (sc == null) {
            sc = Helper.connect(peer, ClusterConfig.HANDSHAKE_TIMEOUT);
        }
        //TODO try again in order to avoid the exception caused by Network jitter
        if (sc == null) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to connect to the peer node: {}", Helper.identifier(peer));
            }
            return;
        }
        this.clusterInfo.cacheChannel(identifier, sc);
        try {
            long beginTime = System.currentTimeMillis();
            ByteBuffer writeBuffer = cmd.encode();
            int length;
            while (writeBuffer.hasRemaining()) {
                length = sc.write(writeBuffer);
                if (length > 0) {
                    if (System.currentTimeMillis() - beginTime > ClusterConfig.HANDSHAKE_TIMEOUT) {
                        throw new TiException("Timeout for initial handshake");
                    }
                } else {
                    throw new TiException("Fatal error happens in initial handshake");
                }
                Thread.sleep(1);
            }
            // the first byte is opType, the second byte is the length of the cmd
            int size = writeBuffer.get(1) & 0xff;
            final ByteBuffer readBuffer = ByteBuffer.allocate(size);
            while (readBuffer.hasRemaining()) {
                length = sc.read(readBuffer);
                if (length > 0) {
                    if (System.currentTimeMillis() - beginTime > ClusterConfig.HANDSHAKE_TIMEOUT) {
                        throw new TiException("Read timeout for initial handshake");
                    }
                } else {
                    // the end-of-stream, sleep and then break;
                    Thread.sleep(1);
                    break;
                }
                Thread.sleep(1);
            }
            readBuffer.flip();

            if (readBuffer.hasRemaining()) {
                TiCommand response = TiCommand.decode(readBuffer);
                this.clusterInfo.map(Entity.decode(Handshake.class, response.getBody()));
            }
        } catch (Exception e) {
            log.warn("Failed to initial the handshake for node: {}, ex:", Helper.identifier(peer), e);
        }
    }

    @ChannelHandler.Sharable
    class ServerHandler extends SimpleChannelInboundHandler<TiCommand> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TiCommand cmd) throws Exception {
            TinyServer.this.dispatcher.doDispatch(ctx, cmd);
        }
    }


    /**
     * we send a handshake command, and wait for the response to update current cluster map
     */
    class HandshakeTask implements Runnable {

        @Override
        public void run() {
            final ConcurrentMap<String, TiClusterNode> cluster = TinyServer.this.clusterInfo.clusterMap;
            log.info("clusterInfo:{}", TinyServer.this.clusterInfo);
            // we don't need the cluster map info, avoid circular reference
            if (cluster != null) {
                // in current version, we handshake with all nodes that in clusterMap
                // in future, can we just take some nodes info like the Redis Gossip?
                List<TiClusterNode> nodes = new ArrayList<>(cluster.values());
                for (TiClusterNode peer : nodes) {
                    Handshake shake = TinyServer.this.clusterInfo.createHandshakeInfo(peer.getNodeId());
                    TinyServer.this.handshakeToPeerNode(peer.getHost(), TiCommand.handshake().of(shake));
                }
            }
        }
    }
}
