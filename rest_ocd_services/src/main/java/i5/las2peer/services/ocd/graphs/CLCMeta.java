package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.beans.ConstructorProperties;

public class CLCMeta {
    /**
     * Database key of the CLC
     */
    private String key;

    /**
     * The name of the CLC
     */
    private String name = "";

    /**
     * The key of the graph the clc is based on
     */
    private String graphKey;

    /**
     * The name of the graph the clc is based on
     */
    private String graphName;

    /**
     * The key of the cover the clc is based on
     */
    private String coverKey;

    /**
     * The name of the cover the clc is based on
     */
    private String coverName;

    /**
     * The type corresponding to the graph creation log.
     */
    int creationTypeId;

    /**
     * The type corresponding to the graph creation log status.
     */
    int creationStatusId;

    @ConstructorProperties({"key", "name", "graphKey", "graphName", "coverKey", "coverName", "creationTypeId", "creationStatusId"})
    public CLCMeta(String key, String name, String graphKey,
                   String graphName, String coverKey, String coverName, int creationTypeId, int creationStatusId) {
        this.key = key;
        this.name = name;
        this.graphKey = graphKey;
        this.graphName = graphName;
        this.coverKey = coverKey;
        this.coverName = coverName;
        this.creationTypeId = creationTypeId;
        this.creationStatusId = creationStatusId;
    }

    /**
     * Finds and returns name of the cover creation type of the
     * cover to which this meta data belongs.
     * author: Beka
     * @return      Cover creation type name.
     */
    public String getCreationTypeName(){
        return CoverCreationType.lookupType(this.creationTypeId).name();
    }

    /**
     * Finds and returns display name of the creation log of the
     * cover to which this meta data belongs.
     * author: Beka
     * @return      Cover creation log display name.
     */
    public String getCreationTypeDisplayName(){
        return CoverCreationType.lookupType(this.creationTypeId).getDisplayName();
    }

    /**
     * Finds and returns name of the execution status of the
     * cover to which this meta data belongs.
     * author: Beka
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

    public String getCoverKey() {
        return coverKey;
    }

    public void setCoverKey(String coverKey) {
        this.coverKey = coverKey;
    }

    public String getCoverName() {
        return coverName;
    }

    public void setCoverName(String coverName) {
        this.coverName = coverName;
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
        return "CLCMeta{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", graphKey='" + graphKey + '\'' +
                ", graphName='" + graphName + '\'' +
                ", coverKey='" + coverKey + '\'' +
                ", coverName='" + coverName + '\'' +
                ", creationTypeId=" + creationTypeId +
                ", creationStatusId=" + creationStatusId +
                '}';
    }
}
