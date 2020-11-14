package my.linkin.server.processor;

import io.netty.channel.ChannelHandlerContext;
import my.linkin.entity.TiCommand;
import my.linkin.server.TinyServer;

/**
 * @author chunhui.wu
 */
public class VoteProcessor extends CommandProcessor {

    public VoteProcessor(TinyServer server) {
        super(server);
    }

    @Override
    protected void process(ChannelHandlerContext ctx, TiCommand cmd) {
        this.server.getRaft().processVote(ctx, cmd);
    }
}
