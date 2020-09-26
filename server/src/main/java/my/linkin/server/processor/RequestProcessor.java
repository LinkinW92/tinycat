package my.linkin.server.processor;

import io.netty.channel.ChannelHandlerContext;
import my.linkin.entity.TiCommand;
import my.linkin.server.TinyServer;

/**
 * @author chunhui.wu
 */
public class RequestProcessor extends CommandProcessor{
    public RequestProcessor(TinyServer server) {
        super(server);
    }

    @Override
    public void process(ChannelHandlerContext ctx, TiCommand cmd) {

    }
}
