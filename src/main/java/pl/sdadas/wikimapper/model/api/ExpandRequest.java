package pl.sdadas.wikimapper.model.api;

/**
 * @author Sławomir Dadas <sdadas@opi.org.pl>
 */
public class ExpandRequest {

    private Long nodeId;

    private ExpandType expand;

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public ExpandType getExpand() {
        return expand;
    }

    public void setExpand(ExpandType expand) {
        this.expand = expand;
    }
}
