package my.linkin;

import java.net.SocketAddress;

/**
 * @author chunhui.wu
 */
public class TinyAddress extends SocketAddress {
    private String ip;
    private int port;

    /**
     * the identifier of this socket address
     */
    public String identity() {
        return new StringBuilder(ip).append(":").append(port).toString();
    }
}
