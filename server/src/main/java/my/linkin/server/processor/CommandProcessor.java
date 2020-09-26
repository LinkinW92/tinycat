package my.linkin.server.processor;

import io.netty.channel.ChannelHandlerContext;
import my.linkin.entity.TiCommand;
import my.linkin.server.TinyServer;

/**
 * @author chunhui.wu
 */
public abstract class CommandProcessor {

    protected TinyServer server;

    public CommandProcessor(TinyServer server) {
        this.server = server;
    }

    /**
     * Process the command, the implements should catch all exceptions rather than throw it
     *
     * @param ctx
     * @param cmd
     */
    protected abstract void process(ChannelHandlerContext ctx, TiCommand cmd);


}
