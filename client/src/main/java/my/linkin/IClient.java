package my.linkin;

import java.net.SocketAddress;

/**
 * @author chunhui.wu
 * client interface
 */
public interface IClient {

    /**
     * 发送消息
     *
     * @param req
     * @param addr the destination for this req
     * @return true if successful
     */
    boolean send(Request req, SocketAddress addr);

    /**
     * 发送消息超时版本
     *
     * @param req
     * @param addr   the destination for this req
     * @param millis timeout
     * @return true if successful
     */
    boolean send(Request req, SocketAddress addr, Long millis);
}
