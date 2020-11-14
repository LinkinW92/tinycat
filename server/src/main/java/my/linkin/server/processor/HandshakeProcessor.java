package my.linkin.server.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import my.linkin.cluster.TiClusterInfo;
import my.linkin.cluster.Handshake;
import my.linkin.entity.Entity;
import my.linkin.entity.TiCommand;
import my.linkin.server.TinyServer;

/**
 * @author chunhui.wu
 */
@Slf4j
public class HandshakeProcessor extends CommandProcessor {

    public HandshakeProcessor(TinyServer server) {
        super(server);
    }

    @Override
    public void process(ChannelHandlerContext ctx, TiCommand cmd) {
        try {
            final TiClusterInfo clusterInfo = super.server.getClusterInfo();
            Handshake shake = Entity.decode(Handshake.class, cmd.getBody());
            clusterInfo.map(shake);
            ctx.channel()
                    .writeAndFlush(TiCommand.handshake().of(clusterInfo.createHandshakeInfo(null)))
                    .addListener((Future<? super Void> future) -> {
                                if (!future.isSuccess()) {
                                    log.warn("Response to handshake command failed");
                                }
                            }
                    );
        } catch (Exception e) {
            log.warn("handshake response exception;{}, cmd:{}", e, cmd);
        }

    }
}
