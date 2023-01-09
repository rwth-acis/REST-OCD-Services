package i5.las2peer.services.ocd.graphs;

/**
 * Composite persistence id of a cover.
 * @author Sebastian
 *
 */
public class CoverId {

	/**
	 * The cover-specific key.
	 */
    private String key;
    
    /**
     * The id of the graph the cover is based on.
     */
    private CustomGraphId graph;
 
    /**
     * Creates a new instance.
     * @param key The cover-specific id.
     * @param graphId The id of the graph the cover is based on.
     */
    public CoverId(String key, CustomGraphId graphId) {
        this.key = key;
        this.graph = graphId;
    }
 
    @Override
    public boolean equals(Object object) {
        if (object instanceof CoverId) {
        	CoverId pk = (CoverId)object;
            return graph.equals(graph) && key.equals(pk.key);
        } else {
            return false;
        }
    }
 
    @Override
    public int hashCode() {
        return (int)(key.hashCode() + graph.hashCode());
    }
	
    /**
     * Getter for the graph id.
     * @return The graph id.
     */
    public CustomGraphId getGraphId() {
    	return graph;
    }
    public String getKey() {
    	return key;
    }
}
