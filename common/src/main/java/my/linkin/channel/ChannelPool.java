package my.linkin.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import my.linkin.ex.TiException;
import my.linkin.util.Helper;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author chunhui.wu
 * a pool for channel reuse
 */
public class ChannelPool {
    /**
     * the channel cache, for reusing purpose
     * key for ip:port address
     */
    private ConcurrentMap<String, ChannelFuture> channelPool = new ConcurrentHashMap<>(16);

    /**
     * the netty bootstrap
     */
    private Bootstrap bootstrap;

    /**
     * the maximum number of channel that the client can create
     */
    private Semaphore maxConnection;

    public ChannelPool(Bootstrap bootstrap, Integer maxConnection) {
        this.bootstrap = bootstrap;
        this.maxConnection = new Semaphore(maxConnection);
    }


    /**
     * open the channel that the addr bind, if not cache, then create a new one
     */
    public ChannelFuture open(SocketAddress addr, Long millis) {
        String identifier = Helper.identifier(addr);
        ChannelFuture cached = this.channelPool.get(identifier);
        if (cached != null) {
            return cached;
        }
        // if we cannot get a channel from the pool , then we create a new one
        tryAcquire(addr, millis);
        ChannelFuture cf = bootstrap.bind(addr).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (!future.isSuccess()) {
                    throw new TiException("Make sure the remote server is alive...");
                }
            }
        });
        // cache the channel for future use
        this.channelPool.putIfAbsent(identifier, cf);
        return cf;
    }

    /**
     * close a channel, and release the permit
     */
    public boolean close(SocketAddress addr) {
        String identifier = Helper.identifier(addr);
        ChannelFuture removed = this.channelPool.remove(identifier);
        if (removed != null) {
            //TODO check the impl
            removed.cancel(true);
            this.maxConnection.release();
        }
        return true;
    }


    /**
     * try acquire the connection
     */
    private boolean tryAcquire(SocketAddress addr, Long millis) {
        try {
            boolean acquired = this.maxConnection.tryAcquire(millis, TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new TiException("Cannot acquire connection after " + millis + " milliseconds");
            }
            return true;
        } catch (InterruptedException ie) {
            // just interrupt
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
