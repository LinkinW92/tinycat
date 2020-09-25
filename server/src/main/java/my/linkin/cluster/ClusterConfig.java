package my.linkin.cluster;

import lombok.Data;
import my.linkin.server.ServerConfig;

/**
 * @author chunhui.wu
 */
@Data
public class ClusterConfig extends ServerConfig {

    /**
     * The rate between nodes is 2 times per minute. Here we take a assumption that
     * the rate is fixed, so when a node lost 2 heartbeat in 1 minute from the other one ,
     * then the node marked the other node as  {@link my.linkin.cluster.TiClusterNode.State#O_OFFLINE}
     */
    public static final int KEEP_ALIVE_MILLIS = 60_000;


    /**
     * In cluster mode, each node should exchange some nodes' info with peer node, this parameter
     * point out the number of the node's info which each node could take. In our test environment, we have 3 servers
     * in TiCluster, so we need take 2 nodes' info for exchanging.
     */
    public static final int FAN_OUT = 2;
}
