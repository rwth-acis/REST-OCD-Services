package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.beans.ConstructorProperties;
import java.util.ArrayList;

/**
 * Instance of this class holds meta information about graphs and is used
 * for efficient requests that don't require accessing full graph
 */
public class CustomGraphMeta {

    /**
     * database key of the CustomGraph to which metadata belongs
     */
    private String key;

    /**
     * id of the CustomGraph to which metadata belongs
     */
    private long id; //TODO: is this needed?

    /**
     * The name of the user owning the graph.
     */
    private String userName = "";

    /**
     * The name of the graph.
     */
    private String name = "";

    /**
     * The node count of the graph.
     */
    private long nodeCount;

    /**
     * The edge count of the graph.
     */
    private long edgeCount;

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
     * @param nodeCount         Node count of the graph
     * @param edgeCount         Edge count of the graph
     * @param types             Array of graph types
     * @param creationTypeId    Id of the graph creation log
     * @param creationStatusId  Status of the graph creation log
     */
    @ConstructorProperties({"key","userName","name","nodeCount","edgeCount", "types", "creationTypeId", "creationStatusId"})
    public CustomGraphMeta(String key, String userName, String name, Long nodeCount, Long edgeCount, ArrayList<Integer> types, int creationTypeId, int creationStatusId) {
        this.key = key;
        this.userName = userName;
        this.name = name;
        this.nodeCount = nodeCount;
        this.edgeCount = edgeCount;
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

    public long getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(long nodeCount) {
        this.nodeCount = nodeCount;
    }

    public long getEdgeCount() {
        return edgeCount;
    }

    public void setEdgeCount(long edgeCount) {
        this.edgeCount = edgeCount;
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
        return "CustomGraphMeta{" +
                "key=" + key +
                ", userName='" + userName + '\'' +
                ", name='" + name + '\'' +
                ", nodeCount=" + nodeCount +
                ", edgeCount=" + edgeCount +
                ", types=" + types +
                ", creationType=" + creationTypeId +
                ", creationStatus=" + creationStatusId +
                '}';
    }
}
