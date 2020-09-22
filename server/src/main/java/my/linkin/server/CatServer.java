package my.linkin.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chunhui.wu
 */
@Slf4j
public class CatServer {

    private int port;
    /**
     * server bind失败时，最大的重试次数
     */
    private int maxRetryLimit = 5;

    public CatServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
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
}
