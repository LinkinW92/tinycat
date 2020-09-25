package my.linkin.cluster;

import lombok.Data;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author chunhui.wu
 */
@Data
public class TiClusterNode {

    public enum Role {
        /**
         * the master, support for read and write operation
         */
        MASTER,
        /**
         * the follower of the cluster, only support for read operation
         */
        FOLLOWER,
        /**
         * the node with this role when a tinyCluster startup, or a master has shutdown. So this state means looking
         * for a new master or become a new master itself
         */
        LOOKING
    }

    public enum State {
        /**
         * the normal state of a node
         */
        ONLINE,
        /**
         * O_OFFLINE short for objective offline, when a node cannot make a handshake with
         * the other one in {@link my.linkin.cluster.ClusterConfig#KEEP_ALIVE_MILLIS}, the node
         * mark the other one as this state.
         */
        O_OFFLINE,
        /**
         * if more than half of the nodes in cluster mark one node as {@link State#O_OFFLINE}, then the node
         * become {@link State#S_OFFLINE}
         */
        S_OFFLINE
    }

    /**
     * the default role for a node
     */
    private Role role = Role.LOOKING;

    /**
     * the default state for a node
     */
    private State state = State.ONLINE;

    /**
     * id for current node
     */
    private String nodeId = UUID.randomUUID().toString().replace("-", "");

    /**
     * the current node host
     */
    private InetSocketAddress host;

    /**
     * last handshake time
     */
    private long lastBeatTime;


    /**
     * handshake map, we use the {@link TiClusterNode#nodeId} as the key
     */
    private ConcurrentMap<String, TiClusterNode> clusterMap = new ConcurrentHashMap<>();

    public TiClusterNode() {
    }

    public TiClusterNode(String nodeId, Role role, InetSocketAddress host) {
        this.nodeId = nodeId;
        this.role = role;
        this.host = host;
        this.lastBeatTime = System.currentTimeMillis();
    }


    /**
     * map the current node to the cluster map. If the node has in the map, just update.
     */
    public void map(TiClusterNode peer) {
        final String nodeId = peer.getNodeId();
        this.clusterMap.put(nodeId, peer);
    }
}
