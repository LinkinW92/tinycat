package my.linkin.cluster;

import my.linkin.server.ServerConfig;

import java.net.InetSocketAddress;

/**
 * @author chunhui.wu
 */
public interface ClusterConfig extends ServerConfig {

    /**
     * The rate between nodes is 2 times per minute. Here we take a assumption that
     * the rate is fixed, so when a node lost 2 heartbeat in 1 minute from the other one ,
     * then the node marked the other node as  {@link my.linkin.cluster.TiClusterNode.State#O_OFFLINE}
     */
    int KEEP_ALIVE_MILLIS = 60_000;


    /**
     * In cluster mode, each node should exchange some nodes' info with peer node, this parameter
     * point out the number of the node's info which each node could take. In our test environment, we have 3 servers
     * in TiCluster, so we need take 2 nodes' info for exchanging.
     */
    int FAN_OUT = 2;


    /**
     * the cluster size should be an odd number. 15 is enough for a test environment
     */
    int CLUSTER_SIZE = 15;


    /**
     * the cluster nodes
     */
    InetSocketAddress[] CLUSTER_NODES = {
            new InetSocketAddress("127.0.0.1", 1000),
            new InetSocketAddress("127.0.0.1", 2000),
//            new InetSocketAddress("127.0.0.1", 3000)
    };

    /**
     * the timeout at milliseconds for the initial handshake
     */
    int HANDSHAKE_TIMEOUT = 5000;
}
