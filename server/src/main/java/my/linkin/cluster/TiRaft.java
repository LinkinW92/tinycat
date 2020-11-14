package my.linkin.cluster;


import io.netty.channel.ChannelHandlerContext;
import my.linkin.cluster.election.Vote;
import my.linkin.entity.TiCommand;
import my.linkin.server.TinyServer;

/**
 * @author chunhui.wu
 * We implements Raft by three steps.
 * Step1: leader election at the boostrap
 * Step2: leader election when master node crash down
 * Step3: data replication from master to followers
 */
public class TiRaft {

    /**
     * the cluster info from current server node view
     */
    private TinyServer server;


    public TiRaft(TinyServer server) {
        this.server = server;
    }

    /**
     * Trigger a election at the server bootstrap
     */
    public void bootstrapElection() {
        // First we span for random time
        try {
            Thread.sleep(((Double) (Math.random() * ClusterConfig.MAX_SPAN_TIMEOUT_MILLIS)).intValue());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        final TiClusterInfo cluster = server.getClusterInfo();
        TiCommand cmd = TiCommand.vote().of(Vote.atBootstrap(cluster.getNode().getNodeId()));
        for (TiClusterNode peer : cluster.getClusterMap().values()) {
            this.server.sendCommand(peer.getHost(), cmd);
        }

    }

    /**
     * If the current node has followed a master, then we just refuse to vote.
     * If the current vote
     *
     * */
    public void processVote(ChannelHandlerContext ctx, TiCommand cmd) {
    }
}