package i5.las2peer.services.ocd.graphs;

/**
 * Composite persistence id of a custom edge.
 * @author Sebastian
 *
 */
public class CustomEdgeId {

	/**
	 * The edge-specific id.
	 */
    private int id;
    
    /**
     * The id of the graph that the edge is part of.
     */
    private CustomGraphId graph;
 
    /**
     * Creates a new instance.
     * @param id The edge-specific id.
     * @param graphId The id of the graph that the edge is part of.
     */
    public CustomEdgeId(int id, CustomGraphId graphId) {
        this.id = id;
        this.graph = graphId;
    }
 
    @Override
    public boolean equals(Object object) {
        if (object instanceof CustomEdgeId) {
        	CustomEdgeId pk = (CustomEdgeId)object;
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
