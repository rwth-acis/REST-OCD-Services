package i5.las2peer.services.ocd.centrality.data;

import i5.las2peer.services.ocd.graphs.CustomGraphId;

/**
 * Composite persistence id of a CentralityMap.
 *
 */
public class CentralityMapId {

	/**
	 * The map-specific key.
	 */
    private String key;
    
    /**
     * The id of the graph the CentralityMap is based on.
     */
    private CustomGraphId graph;
 
    /**
     * Creates a new instance.
     * @param key The map-specific key.
     * @param graphId The id of the graph the CentralityMap is based on.
     */
    public CentralityMapId(String key, CustomGraphId graphId) {
        this.key = key;
        this.graph = graphId;
    }
 
    @Override
    public boolean equals(Object object) {
        if (object instanceof CentralityMapId) {
        	CentralityMapId pk = (CentralityMapId)object;
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
    /**
     * Getter for the map key.
     * @return The map key.
     */
    public String getKey() {
    	return key;
    }
}
