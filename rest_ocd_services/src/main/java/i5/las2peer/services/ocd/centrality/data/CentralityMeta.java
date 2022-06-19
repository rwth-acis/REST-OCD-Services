package i5.las2peer.services.ocd.centrality.data;

/**
 * Instance of this class holds meta information about a centrality and is used
 * for efficient requests that don't require accessing full centrality data
 */
public class CentralityMeta {

    /**
     * The id of the CentralityMap.
     */
    Long centralityId;

    /**
     * The name of the CentralityMap.
     */
    String centralityName;

    /**
     * The creation log of the CentralityMap.
     */
    CentralityCreationLog centralityCreationLog;

    /**
     * The id of the graph centrality is based on.
     */
    Long graphId;

    /**
     * The name of the graph centrality is based on.
     */
    String graphName;


    /**
     * The node count of the graph centrality is based on.
     */
    Long graphSize;


    public CentralityMeta(Long centralityId, String centralityName, CentralityCreationLog centralityCreationLog, Long graphId, String graphName, Long graphSize) {
        this.centralityId = centralityId;
        this.centralityName = centralityName;
        this.centralityCreationLog = centralityCreationLog;
        this.graphId = graphId;
        this.graphName = graphName;
        this.graphSize = graphSize;

    }

    public Long getCentralityId() {
        return centralityId;
    }

    public void setCentralityId(Long centralityId) {
        this.centralityId = centralityId;
    }

    public String getCentralityName() {
        return centralityName;
    }

    public void setCentralityName(String centralityName) {
        this.centralityName = centralityName;
    }

    public CentralityCreationLog getCentralityCreationLog() {
        return centralityCreationLog;
    }

    public void setCentralityCreationLog(CentralityCreationLog centralityCreationLog) {
        this.centralityCreationLog = centralityCreationLog;
    }

    public Long getGraphId() {
        return graphId;
    }

    public void setGraphId(Long graphId) {
        this.graphId = graphId;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public Long getGraphSize() {
        return graphSize;
    }

    public void setGraphSize(Long graphSize) {
        this.graphSize = graphSize;
    }

    @Override
    public String toString() {
        return "CentralityMeta{" +
                "centralityId=" + centralityId +
                ", centralityName='" + centralityName + '\'' +
                ", centralityCreationLog=" + centralityCreationLog.getStatus() +
                ", graphId=" + graphId +
                ", graphName='" + graphName + '\'' +
                ", graphSize=" + graphSize +
                '}';
    }
}
