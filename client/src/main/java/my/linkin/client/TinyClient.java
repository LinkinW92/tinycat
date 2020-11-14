package my.linkin.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import my.linkin.AutoChannelRemovalHandler;
import my.linkin.Config;
import my.linkin.IClient;
import my.linkin.channel.ChannelPool;
import my.linkin.codec.TiDecoder;
import my.linkin.codec.TiEncoder;
import my.linkin.entity.*;
import my.linkin.ex.TiException;
import my.linkin.thread.TiThreadFactory;
import my.linkin.util.Helper;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chunhui.wu
 */
@Slf4j
public class TinyClient implements IClient {

    /**
     * the netty bootstrap
     */
    private Bootstrap bootstrap;

    private static final Long DEFAULT_TIMEOUT = 1000L;

    /**
     * the executor for some common tasks
     */
    private ExecutorService publicExecutor;

    /**
     * timer for heartbeat
     */
    private Timer timer = new Timer("Heartbeat", true);
    /**
     * if canceled is true, we terminate the timer and won't send heartbeat anymore
     */
    private AtomicBoolean canceled = new AtomicBoolean(false);


    /**
     * the channel pool
     */
    private ChannelPool pool;

    public TinyClient(Integer maxConnection) {
        bootstrap = new Bootstrap();
        //TODO early expose the reference of bootstrap, does it have any problems?
        pool = new ChannelPool(bootstrap, maxConnection, "TinyClient");

        EventLoopGroup worker = new NioEventLoopGroup();
        bootstrap.group(worker)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(5000, 5000, 0, TimeUnit.MILLISECONDS));
                        // auto remove the channel from the channel pool if it becomes idle
                        pipeline.addLast(new AutoChannelRemovalHandler(pool));
                        pipeline.addLast(new TiDecoder(pool));
                        pipeline.addLast(new TiEncoder(pool));
                        pipeline.addLast(new ClientHandler(pool));
                    }
                });
        this.publicExecutor = new ThreadPoolExecutor(Config.publicExecutorCoreSize,
                Config.publicExecutorCoreSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(Config.publicWorkerQueueSize),
                new TiThreadFactory("TinyClient"),
                new ThreadPoolExecutor.CallerRunsPolicy());


        start();
    }

    /**
     * start some helper job， such as heartbeat thread
     */
    private void start() {
        // the heartbeat task runs two times per second
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!canceled.get()) {
                    TinyClient.this.beatIt(new AtomicInteger(Config.maxRetryTimes));
                } else {
                    // terminate
                    timer.cancel();
                    // remove the canceled tasks
                    timer.purge();
                }
            }
        }, 3 * 1000, 30 * 1000);
    }

    private void beatIt(final AtomicInteger retryTimes) {
        InetSocketAddress serverAddr = new InetSocketAddress("localhost", 1010);
        // if we fail to handshake with the server, then we remove the channel which bind with the server addr
        if (retryTimes.get() == 0) {
            log.warn("Failed to send heartbeat to the server:{}, so we have to remove the channel from the pool", Helper.identifier(serverAddr));
            this.pool.close(serverAddr);
            log.info("Shutdown the heartbeat timer...");
            this.canceled.compareAndSet(false, true);
            return;
        }
        ChannelFuture cf = pool.open(serverAddr, 1000L);
        TiCommand cmd = TiCommand.heartbeat();
        Heartbeat hb = Heartbeat.ping();
        cmd.setBody(hb.encode());
        cf.channel().writeAndFlush(cmd).addListener((Future<? super Void> future) -> {
            // if the heartbeat is failed, sleep for a while and then try again
            if (!future.isSuccess()) {
                Thread.sleep((Config.maxRetryTimes - retryTimes.get()) * 1000);
                retryTimes.getAndSet(retryTimes.decrementAndGet());
                publicExecutor.execute(() -> beatIt(retryTimes));
            } else {
            }
        });
    }

    @Override
    public boolean send(TiCommand req, SocketAddress addr) {
        ChannelFuture cf = this.pool.open(addr, DEFAULT_TIMEOUT);
        return this.doPrivateSend(cf, req, Config.maxRetryTimes);
    }

    @Override
    public boolean send(TiCommand req, SocketAddress addr, Long millis) {
        ChannelFuture cf = this.pool.open(addr, millis);
        return this.doPrivateSend(cf, req, Config.maxRetryTimes);
    }

    private boolean doPrivateSend(final ChannelFuture cf, TiCommand req, Integer retryTimes) {
        if (retryTimes < 0) {
            throw new TiException("Fail to send req after max retry times");
        }
        final int retry = --retryTimes;
        cf.channel().writeAndFlush(req).addListener((ChannelFuture future) -> {
            if (!future.isSuccess()) {
                doPrivateSend(cf, req, retry);
            } else {
                log.info("Send req successfully with retry times:{}", Config.maxRetryTimes - retry + 1);
            }
        });
        return true;
    }

    @ChannelHandler.Sharable
    class ClientHandler extends SimpleChannelInboundHandler<TiCommand> {

        private ChannelPool pool;

        public ClientHandler(ChannelPool pool) {
            this.pool = pool;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, TiCommand cmd) throws Exception {
            final Header h = cmd.getHeader();
            final OpType op = h.getOpType();
            switch (op) {
                case HEARTBEAT:
                    final Heartbeat beat;
                    if (h.getLength() > 0) {
                        beat = Entity.decode(Heartbeat.class, cmd.getBody());
                        log.info("{}", beat.getMessage());
                    }
                    break;
                default:
                    log.warn("Leave to be finished in the future");
            }
        }
    }
}
