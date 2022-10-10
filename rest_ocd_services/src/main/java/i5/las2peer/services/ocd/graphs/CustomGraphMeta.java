package i5.las2peer.services.ocd.graphs;

import java.util.ArrayList;

/**
 * Instance of this class holds meta information about graphs and is used
 * for efficient requests that don't require accessing full graph
 */
public class CustomGraphMeta {

    /**
     * id of the CustomGraph to which metadata belongs
     */
    private long persistenceId;

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
     * The graph creation log of the graph.
     */
    GraphCreationLog graphCreationLog;

    public CustomGraphMeta(long persistenceId) {
        this.persistenceId = persistenceId;
        System.out.println("creating with id = "+ persistenceId);
    }

    public CustomGraphMeta(long persistenceId, String userName, String name, long nodeCount, long edgeCount, ArrayList<Integer> types, GraphCreationLog graphCreationLog) {
        this.persistenceId = persistenceId;
        this.userName = userName;
        this.name = name;
        this.nodeCount = nodeCount;
        this.edgeCount = edgeCount;
        this.graphCreationLog = graphCreationLog;

        if(types != null) {
            this.types = types;
        }else{
            this.types = new ArrayList<>();
        }

    }

    public long getPersistenceId() {
        return persistenceId;
    }

    public void setPersistenceId(long persistenceId) {
        this.persistenceId = persistenceId;
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

    public GraphCreationLog getGraphCreationLog() {
        return graphCreationLog;
    }

    public void setGraphCreationLog(GraphCreationLog graphCreationLog) {
        this.graphCreationLog = graphCreationLog;
    }

    @Override
    public String toString() {
        return "CustomGraphMeta{" +
                "id=" + persistenceId +
                ", userName='" + userName + '\'' +
                ", name='" + name + '\'' +
                ", nodeCount=" + nodeCount +
                ", edgeCount=" + edgeCount +
                ", types=" + types +
                '}';
    }
}
