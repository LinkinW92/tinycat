package my.linkin.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author chunhui.wu
 * cat client, 向server端发送消息
 */
public class CatClient {
    int port;

    public CatClient(int port) {
        this.port = port;
    }

    public void start() {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {

                    }
                });
        bootstrap.connect("127.0.0.1", port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("connect to cat server successfully");
            } else {
                System.out.println("connect to cat server failed");
            }
        });
    }
}
