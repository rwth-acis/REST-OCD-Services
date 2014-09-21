package i5.las2peer.services.ocd.graphs;

/**
 * Composite persistence id of a node.
 * @author Sebastian
 *
 */
public class CustomNodeId {

	/**
	 * The node-specific id.
	 */
    private int id;
    
    /**
     * The id of the graph that the node is part of.
     */
    private CustomGraphId graph;
 
    /**
     * Creates a new instance.
     * @param id The node-specific id.
     * @param graphId The id of the graph that the node is part of.
     */
    public CustomNodeId(int id, CustomGraphId graphId) {
        this.id = id;
        this.graph = graphId;
    }
 
    @Override
    public boolean equals(Object object) {
        if (object instanceof CustomNodeId) {
        	CustomNodeId pk = (CustomNodeId)object;
            return graph.equals(graph) && id == pk.id;
        } else {
            return false;
        }
    }
 
    @Override
    public int hashCode() {
        return (int)(id + graph.hashCode());
    }
	
}
