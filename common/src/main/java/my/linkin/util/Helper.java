package my.linkin.util;

import lombok.extern.slf4j.Slf4j;
import my.linkin.Config;
import my.linkin.ex.TiException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @author chunhui.wu
 */
@Slf4j
public class Helper {

    /**
     * get the identifier of InetSocketAddress
     *
     * @param sa
     * @return ip:port
     */
    public static String identifier(SocketAddress sa) {
        if (!(sa instanceof InetSocketAddress)) {
            throw new TiException("Unknown socketAddress...");
        }
        InetSocketAddress isa = (InetSocketAddress) sa;
        return new StringBuilder(isa.getHostName()).append(":").append(isa.getPort()).toString();
    }

    /**
     * change the ip:port to InetSocketAddress
     *
     * @param identifier ip:port
     * @return
     */
    public static InetSocketAddress toISA(String identifier) {
        String[] addr = identifier.split(":");
        return new InetSocketAddress(addr[0], Integer.valueOf(addr[1]));
    }

    /**
     * for a local test environment, the host is fake
     */
    public static InetSocketAddress fakeAddr(int port) {
        try {
            return new InetSocketAddress(Config.HOST, port);
        } catch (Exception e) {
            log.warn("Cannot get local host, ex: {}", e);
            throw new TiException("Failed to get local host");
        }
    }


    /**
     * connect to the other node
     */
    public static SocketChannel connect(final InetSocketAddress peer, final int connectTimeout) {
        SocketChannel sc = null;
        try {
            sc = SocketChannel.open();
            sc.configureBlocking(true);
            sc.socket().setKeepAlive(true);
            sc.socket().setSoLinger(false, -1);
            sc.socket().setTcpNoDelay(true);
            sc.socket().setSendBufferSize(64 * 1024);
            sc.socket().setReceiveBufferSize(64 * 1024);
            sc.socket().connect(peer, connectTimeout);
            sc.configureBlocking(false);
            return sc;
        } catch (Exception e) {
            if (sc != null) {
                try {
                    sc.close();
                } catch (IOException ioe) {
                    log.warn("Closing socket channel failed, ex:{}", ioe);
                }
            }
        }
        return null;
    }
}
