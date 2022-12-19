package i5.las2peer.services.ocd.centrality.data;

import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.beans.ConstructorProperties;

/**
 * Instance of this class holds meta information about a centrality and is used
 * for efficient requests that don't require accessing full centrality data
 */
public class CentralityMeta {

    /**
     * The key of the CentralityMap.
     */
    private String centralityKey;

    /**
     * The name of the CentralityMap.
     */
    private String centralityName;

    /**
     * The key of the graph centrality is based on.
     */
    private String graphKey;

    /**
     * The name of the graph centrality is based on.
     */
    private String graphName;


    /**
     * The type corresponding to the centrality creation log.
     */
    private int creationTypeId;

    /**
     * The type corresponding to the graph creation log status.
     */
    private int creationStatusId;

    /**
     * The execution time of the centrality in ms.
     */
    private long executionTime;

    @ConstructorProperties({"centralityKey","centralityName","graphKey","graphName", "creationTypeId", "creationStatusId", "executionTime"})
    public CentralityMeta(String centralityKey, String centralityName, String graphKey, String graphName,
                          int creationTypeId, int creationStatusId, long executionTime) {
        this.centralityKey = centralityKey;
        this.centralityName = centralityName;
        this.graphKey = graphKey;
        this.graphName = graphName;
        this.creationTypeId = creationTypeId;
        this.creationStatusId = creationStatusId;
        this.executionTime = executionTime;
    }

    /**
     * Finds and returns name of the centrality creation type of the
     * centrality to which this meta data belongs.
     * @return      Centrality creation type name.
     */
    public String getCreationTypeName(){
        return CentralityCreationType.lookupType(this.creationTypeId).name();
    }

    /**
     * Finds and returns display name of the centrality creation log of the
     * centrality to which this meta data belongs.
     * @return      Centrality creation log display name.
     */
    public String getCreationTypeDisplayName(){
        return CentralityCreationType.lookupType(this.creationTypeId).getDisplayName();
    }

    /**
     * Finds and returns name of the execution status of the
     * centrality to which this meta data belongs.
     * @return      Centrality execution status name.
     */
    public String getCreationStatusName(){

        return ExecutionStatus.lookupStatus(this.creationStatusId).name();
    }

    public String getCentralityKey() {
        return centralityKey;
    }

    public void setCentralityKey(String centralityKey) {
        this.centralityKey = centralityKey;
    }

    public String getCentralityName() {
        return centralityName;
    }

    public void setCentralityName(String centralityName) {
        this.centralityName = centralityName;
    }

    public String getGraphKey() {
        return graphKey;
    }

    public void setGraphKey(String graphKey) {
        this.graphKey = graphKey;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public int getCreationTypeId() {
        return creationTypeId;
    }

    public void setCreationTypeId(int creationTypeId) {
        this.creationTypeId = creationTypeId;
    }

    public int getCreationStatusId() {
        return creationStatusId;
    }

    public void setCreationStatusId(int creationStatusId) {
        this.creationStatusId = creationStatusId;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    @Override
    public String toString() {
        return "CentralityMeta{" +
                "centralityKey='" + centralityKey + '\'' +
                ", centralityName='" + centralityName + '\'' +
                ", graphKey='" + graphKey + '\'' +
                ", graphName='" + graphName + '\'' +
                ", creationTypeId=" + creationTypeId +
                ", creationStatusId=" + creationStatusId +
                ", executionTime=" + executionTime +
                '}';
    }

}
