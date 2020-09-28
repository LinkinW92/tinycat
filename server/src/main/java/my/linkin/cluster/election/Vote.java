package my.linkin.cluster.election;

import lombok.Data;
import my.linkin.cluster.TiClusterNode;
import my.linkin.entity.Entity;

/**
 * @author chunhui.wu
 */
@Data
public class Vote extends Entity {

    /**
     * the epoch for current vote
     */
    private Epoch epoch;
    /**
     * the nodeId (see {@link TiClusterNode#getNodeId()}) that the voter cast vote to
     */
    private String candidate;
    /**
     * the nodeId of the voter
     */
    private String voter;

    public static Vote atBootstrap(String nodeId) {
        Vote v = new Vote();
        v.setCandidate(nodeId);
        v.setVoter(nodeId);
        v.setEpoch(Epoch.createEpoch());
        return v;
    }
}
