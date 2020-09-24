package my.linkin.util;

import my.linkin.ex.TiException;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author chunhui.wu
 */
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
}
