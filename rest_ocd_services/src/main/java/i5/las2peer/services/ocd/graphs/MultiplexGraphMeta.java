package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.beans.ConstructorProperties;
import java.util.ArrayList;

/**
 * Instance of this class holds meta information about graphs and is used
 * for efficient requests that don't require accessing full graph
 */
public class MultiplexGraphMeta {

    /**
     * database key of the MultiplexGraph to which metadata belongs
     */
    private String key;

    /**
     * id of the MultiplexGraph to which metadata belongs
     */
    private long id;

    /**
     * The name of the user owning the graph.
     */
    private String userName = "";

    /**
     * The name of the graph.
     */
    private String name = "";

    /**
     * The layer count of the graph.
     */
    private int layerCount;

    /**
     * The list of type enum ids of the graph.
     */
    ArrayList<Integer> types;

    /**
     * The type corresponding to the graph creation log.
     */
    int creationTypeId;

    /**
     * The type corresponding to the graph creation log status.
     */
    int creationStatusId;


    /**
     * Constructor that is used to generate a CustomGraphMeta instance
     * using the JSON input resulting from ArangoDB queries
     *
     * @param key               Key of the graph
     * @param userName          Creator of the graph
     * @param name              Name of the graph
     * @param layerCount         Node count of the graph
     * @param types             Array of graph types
     * @param creationTypeId    Id of the graph creation log
     * @param creationStatusId  Status of the graph creation log
     */
    @ConstructorProperties({"key","userName","name","nodeCount","edgeCount", "types", "creationTypeId", "creationStatusId"})
    public MultiplexGraphMeta(String key, String userName, String name, int layerCount, ArrayList<Integer> types, int creationTypeId, int creationStatusId) {
        this.key = key;
        this.userName = userName;
        this.name = name;
        this.layerCount = layerCount;
        this.creationTypeId = creationTypeId;
        this.creationStatusId = creationStatusId;

       if(types != null) {
            this.types =  types;
        }else{
            this.types = new ArrayList<>();
        }
    }

    public String getKey() {return key;}

    public void setKey(String key) {this.key = key;}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLayerCount() {
        return layerCount;
    }

    public void setLayerCount(int layerCount) {
        this.layerCount = layerCount;
    }

    public ArrayList<Integer> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<Integer> types) {
        this.types = types;
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

    /**
     * Finds and returns name of the graph creation type of the
     * graph to which this meta data belongs.
     * @return      Graph creation type name.
     */
    public String getCreationTypeName(){
        return GraphCreationType.lookupType(this.creationTypeId).name();
    }

    /**
     * Finds and returns display name of the creation log s of the
     * graph to which this meta data belongs.
     * @return      Graph creation log display name.
     */
    public String getCreationTypeDisplayName(){
        return GraphCreationType.lookupType(this.creationTypeId).getDisplayName();
    }

    /**
     * Finds and returns name of the execution status of the
     * graph to which this meta data belongs.
     * @return      Graph execution status name.
     */
    public String getCreationStatusName(){
        return ExecutionStatus.lookupStatus(this.creationStatusId).name();
    }



    @Override
    public String toString() {
        return "MultiplexGraphMeta{" +
                "key=" + key +
                ", userName='" + userName + '\'' +
                ", name='" + name + '\'' +
                ", nodeCount=" + layerCount +
                ", types=" + types +
                ", creationType=" + creationTypeId +
                ", creationStatus=" + creationStatusId +
                '}';
    }
}
