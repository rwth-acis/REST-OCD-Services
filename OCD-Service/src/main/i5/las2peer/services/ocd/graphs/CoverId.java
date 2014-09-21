package i5.las2peer.services.ocd.graphs;

/**
 * Composite persistence id of a cover.
 * @author Sebastian
 *
 */
public class CoverId {

	/**
	 * The cover-specific id.
	 */
    private long id;
    
    /**
     * The id of the graph the cover is based on.
     */
    private CustomGraphId graph;
 
    /**
     * Creates a new instance.
     * @param id The cover-specific id.
     * @param graphId The id of the graph the cover is based on.
     */
    public CoverId(long id, CustomGraphId graphId) {
        this.id = id;
        this.graph = graphId;
    }
 
    @Override
    public boolean equals(Object object) {
        if (object instanceof CoverId) {
        	CoverId pk = (CoverId)object;
            return graph.equals(graph) && id == pk.id;
        } else {
            return false;
        }
    }
 
    @Override
    public int hashCode() {
        return (int)(id + graph.hashCode());
    }
	
    /**
     * Getter for the graph id.
     * @return The graph id.
     */
    public CustomGraphId getGraphId() {
    	return graph;
    }
}
