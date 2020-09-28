package my.linkin.server.processor;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import my.linkin.entity.OpType;
import my.linkin.entity.TiCommand;
import my.linkin.server.TinyServer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author chunhui.wu
 * Dispatch the command to the relative processor.
 */
@Slf4j
public class CommandDispatcher {
    /**
     * the factory for command processors with initial capacity 8. 8 is enough for current version
     */
    private ConcurrentMap<OpType, CommandProcessor> processorFactory = new ConcurrentHashMap<>(8);


    private TinyServer server;

    public CommandDispatcher(TinyServer server) {
        this.server = server;
    }

    public void doDispatch(ChannelHandlerContext ctx, TiCommand cmd) {
        OpType op = cmd.getHeader().getOpType();
        final CommandProcessor processor = this.processorFactory.get(op);
        if (processor == null) {
            log.error("No relative command processor found for current opType: {}", op.name());
            return;
        }
        if (cmd.getHeader().getLength() == 0) {
            if (log.isDebugEnabled()) {
                log.warn("No effective data found on body for opType:{}", op.name());
            }
            return;
        }
        processor.process(ctx, cmd );
    }

    /**
     * register the processors
     */
    public void init() {
        this.processorFactory.putIfAbsent(OpType.HANDSHAKE, new HandshakeProcessor(server));
        this.processorFactory.putIfAbsent(OpType.HEARTBEAT, new HeartbeatProcessor(server));
        this.processorFactory.putIfAbsent(OpType.REQUEST, new RequestProcessor(server));
        this.processorFactory.putIfAbsent(OpType.RESPONSE, new ResponseProcessor(server));
        this.processorFactory.putIfAbsent(OpType.VOTE, new VoteProcessor(server));
    }
}
