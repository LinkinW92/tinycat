package my.linkin.cluster;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import my.linkin.ex.TiException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author chunhui.wu
 */
@Data
@Slf4j
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


    public TiClusterNode() {
    }

    public TiClusterNode(Role role, InetSocketAddress host) {
        this.role = role;
        this.host = host;
        this.lastBeatTime = System.currentTimeMillis();
    }


    /**
     * deep copy for current node
     */
    public TiClusterNode deepCopy() {
        TiClusterNode node = new TiClusterNode();
        node.lastBeatTime = System.currentTimeMillis();
        node.nodeId = this.nodeId;
        node.host = this.host;
        node.role = this.role;
        node.state = this.state;
        return node;
    }


    /**
     * if a postponed handshake happened, we wont update the node info
     */
    public void update(TiClusterNode updater) {
        if (updater.lastBeatTime < this.lastBeatTime) {
            return;
        }
        this.lastBeatTime = updater.lastBeatTime;
        this.state = State.ONLINE;
        this.role = updater.getRole();
        // ip may be change after last handshake
        this.host = updater.host;
    }
}
