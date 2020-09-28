package my.linkin.cluster;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import my.linkin.ex.TiException;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author chunhui.wu
 * In cluster mode, we need maintain the current server node info, other nodes info exchanged by handshake,
 * and cache the socket channel for the peer node.
 */
@Slf4j
@Data
public class TiClusterInfo {

    /**
     * the cluster master node, maybe equals to the {@link TiClusterInfo#node}
     */
    public TiClusterNode master;

    /**
     * the cluster node info about current server if the cluster mode is enable
     */
    public TiClusterNode node;
    /**
     * handshake map, we use the {@link TiClusterNode#getNodeId()} as the key
     */
    public ConcurrentMap<String, TiClusterNode> clusterMap = new ConcurrentHashMap<>();

    /**
     * the cache for channels
     */
    public ConcurrentMap<String, SocketChannel> channels = new ConcurrentHashMap<>();

    public Handshake createHandshakeInfo(String excludeNodeId) {
        final ConcurrentMap<String, TiClusterNode> cluster = this.clusterMap;
        final TiClusterNode node = this.node;
        Handshake shake = Handshake.initial(node.deepCopy());
        if (cluster != null) {
            List<TiClusterNode> nodes = new ArrayList<>(cluster.values());
            Map<String, TiClusterNode> forExchange = nodes.parallelStream()
                    .filter(e -> e != null)
                    .filter(e -> isEmpty(excludeNodeId) || !e.getNodeId().equals(excludeNodeId))
                    .limit(ClusterConfig.FAN_OUT)
                    .collect(Collectors.toMap(TiClusterNode::getNodeId, o -> o));
            shake.setExchange(forExchange);

        }
        return shake;
    }

    /**
     * map the current node to the cluster map. If the node has been in the map, just update.
     */
    private void map(TiClusterNode peer) {
        final ConcurrentMap<String, TiClusterNode> cluster = this.clusterMap;
        if (peer.getHost().equals(this.node.getHost())) {
            return;
        }
        final String nodeId = peer.getNodeId();
        if (isEmpty(nodeId)) {
            throw new TiException("Unknown peer node");
        }
        TiClusterNode exist = cluster.get(nodeId);
        if (exist != null) {
            exist.update(peer);
            return;
        }
        peer.setLastBeatTime(System.currentTimeMillis());
        cluster.put(nodeId, peer);
    }

    /**
     * map the nodes exchanged from the handshake to the cluster map. If the node has been in the map, just update.
     */
    public void map(Handshake shake) {
        if (shake == null) {
            if (log.isDebugEnabled()) {
                log.debug("Something error may happen in handshake, no shake info can be used");
            }
            return;
        }
        // the peer node info between one shake
        TiClusterNode peer = shake.getNode();
        map(peer);
        // map the exchange
        if (shake.getExchange() != null) {
            for (Map.Entry<String, TiClusterNode> entry : shake.getExchange().entrySet()) {
                map(entry.getValue());
            }
        }
    }

    public SocketChannel getCachedChannel(String identifier) {
        return this.channels.get(identifier);
    }

    public void cacheChannel(String identifier, SocketChannel sc) {
        this.channels.put(identifier, sc);
    }

    /**
     * There are some abnormal scenes should be took into account, such as:
     * 1.A serer node start up later than others
     * 2.A server node crash and then restart
     * Under the above circumstances, we should do some job to make sure that our servers can connect to a later server node or re-connect to a crashed-then-restart node
     */
    public List<InetSocketAddress> unfinishedHandshakeServers() {
        if (this.clusterMap.size() == ClusterConfig.CLUSTER_NODES.length - 1) {
            return new ArrayList<>();
        }
        List<InetSocketAddress> pivot = Arrays.asList(ClusterConfig.CLUSTER_NODES);
        if (this.clusterMap.size() == 0) {
            return pivot;
        }
        return this.clusterMap.values()
                .stream()
                .filter(e -> e != null)
                .filter(e -> !pivot.contains(e.getHost()))
                .map(TiClusterNode::getHost).collect(Collectors.toList());

    }
}
