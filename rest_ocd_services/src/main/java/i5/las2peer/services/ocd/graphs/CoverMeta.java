package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.beans.ConstructorProperties;
import java.util.ArrayList;

/**
 * Instance of this class holds meta information about covers and is used
 * for efficient requests that don't require accessing full cover
 */
public class CoverMeta {

    /**
     * database key of the Cover to which metadata belongs
     */
    private String key;

    /**
     * The name of the cover
     */
    private String name = "";

    /**
     * The number of communities of the cover
     */
    private long numberOfCommunities;

    /**
     * The key of the graph the cover is based on.
     */
    private String graphKey;

    /**
     * The name of the graph
     */
    private String graphName;

    /**
     * The type corresponding to the graph creation log.
     */
    int creationTypeId;

    /**
     * The type corresponding to the graph creation log status.
     */
    int creationStatusId;


    /**
     *
     * @param key                   Key of the cover
     * @param name                  Name of the cover
     * @param numberOfCommunities   Number of communities in the cover
     * @param graphKey              Key of the graph the cover is based on
     * @param graphName             Name of the graph
     * @param creationTypeId        Id of the cover creation log
     * @param creationStatusId      Status of the Cover creation log
     */
    @ConstructorProperties({"key","name","numberOfCommunities","graphKey", "graphName", "creationTypeId", "creationStatusId"})
    public CoverMeta(String key, String name, long numberOfCommunities, String graphKey,
                     String graphName, int creationTypeId, int creationStatusId) {
        this.key = key;
        this.name = name;
        this.numberOfCommunities = numberOfCommunities;
        this.graphKey = graphKey;
        this.graphName = graphName;
        this.creationTypeId = creationTypeId;
        this.creationStatusId = creationStatusId;
    }


    /**
     * Finds and returns name of the cover creation type of the
     * cover to which this meta data belongs.
     * @return      Cover creation type name.
     */
    public String getCreationTypeName(){
        return CoverCreationType.lookupType(this.creationTypeId).name();
    }

    /**
     * Finds and returns display name of the creation log of the
     * cover to which this meta data belongs.
     * @return      Cover creation log display name.
     */
    public String getCreationTypeDisplayName(){
        return CoverCreationType.lookupType(this.creationTypeId).getDisplayName();
    }

    /**
     * Finds and returns name of the execution status of the
     * cover to which this meta data belongs.
     * @return      Cover execution status name.
     */
    public String getCreationStatusName(){

        return ExecutionStatus.lookupStatus(this.creationStatusId).name();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getNumberOfCommunities() {
        return numberOfCommunities;
    }

    public void setNumberOfCommunities(long numberOfCommunities) {
        this.numberOfCommunities = numberOfCommunities;
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

    @Override
    public String toString() {
        return "CoverMeta{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", numberOfCommunities=" + numberOfCommunities +
                ", graphKey='" + graphKey + '\'' +
                ", graphName='" + graphName + '\'' +
                ", creationTypeId=" + creationTypeId +
                ", creationStatusId=" + creationStatusId +
                '}';
    }
}
