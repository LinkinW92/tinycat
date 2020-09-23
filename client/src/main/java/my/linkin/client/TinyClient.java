package my.linkin.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import my.linkin.AutoClearChannelHander;
import my.linkin.IClient;
import my.linkin.Request;
import my.linkin.channel.ChannelPool;
import my.linkin.ex.TiException;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author chunhui.wu
 */
@Slf4j
public class TinyClient implements IClient {

    /**
     * the netty bootstrap
     */
    private Bootstrap bootstrap;

    private static final Long DEFAULT_TIMEOUT = 1000l;

    /**
     * if we fail to send a req, then we retry until the retry times exceed the {@link TinyClient#MAX_RETRY_LIMIT}
     */
    private static final int MAX_RETRY_LIMIT = 5;


    /**
     * the channel pool
     */
    private ChannelPool pool;

    public TinyClient(Integer maxConnection) {
        bootstrap = new Bootstrap();
        //TODO early expose the reference of bootstrap, does it have any problems?
        pool = new ChannelPool(bootstrap, maxConnection);

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
                        pipeline.addLast(new AutoClearChannelHander(pool));
                    }
                });
    }

    @Override
    public boolean send(Request req, SocketAddress addr) {
        ChannelFuture cf = this.pool.open(addr, DEFAULT_TIMEOUT);
        return doPrivateSend(cf, req, MAX_RETRY_LIMIT);
    }

    @Override
    public boolean send(Request req, SocketAddress addr, Long millis) {
        ChannelFuture cf = this.pool.open(addr, millis);
        return doPrivateSend(cf, req, MAX_RETRY_LIMIT);
    }

    private boolean doPrivateSend(ChannelFuture cf, Request<?> req, Integer retryTimes) {
        if (retryTimes < 0) {
            throw new TiException("Fail to send req after max retry times");
        }
        final int retry = --retryTimes;
        cf.channel().writeAndFlush(req).addListener((ChannelFuture future) -> {
            if (!future.isSuccess()) {
                doPrivateSend(cf, req, retry);
            } else {
                log.info("Send req successfully with retry times:{}", MAX_RETRY_LIMIT - retry + 1);
            }
        });
        return true;
    }
}
