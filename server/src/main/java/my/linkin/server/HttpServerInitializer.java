package my.linkin.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import my.linkin.server.handler.HttpRequestHandler;

/**
 * @author chunhui.wu
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // http 编解码
        pipeline.addLast(new HttpServerCodec());
        // http 消息聚合器 512*1024为接收的最大contentLength
        pipeline.addLast("httpAggregator", new HttpObjectAggregator(512 * 1024));
        pipeline.addLast(new HttpRequestHandler());
    }
}
