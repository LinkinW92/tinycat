package my.linkin.cluster;

import lombok.Data;
import my.linkin.entity.Entity;

import java.util.Map;

/**
 * @author chunhui.wu
 * the handshake entity amongs serevr
 */
@Data
public class Handshake extends Entity {

    /**
     * the promoter node
     */
    private TiClusterNode node;
    /**
     * nodes' info for exchange, see {@link ClusterConfig#FAN_OUT}
     */
    private Map<String, TiClusterNode> exchange;

    public Handshake() {
    }

    public static Handshake initial(TiClusterNode node) {
        final TiClusterNode tn = node.deepCopy();
        Handshake h = new Handshake();
        h.setNode(tn);
        return h;
    }
}
