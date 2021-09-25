package i5.las2peer.services.ocd.centrality.data;

import i5.las2peer.services.ocd.graphs.CustomGraphId;

/**
 * Composite persistence id of a CentralityMap.
 *
 */
public class CentralityMapId {

	/**
	 * The map-specific id.
	 */
    private long id;
    
    /**
     * The id of the graph the CentralityMap is based on.
     */
    private CustomGraphId graph;
 
    /**
     * Creates a new instance.
     * @param id The map-specific id.
     * @param graphId The id of the graph the CentralityMap is based on.
     */
    public CentralityMapId(long id, CustomGraphId graphId) {
        this.id = id;
        this.graph = graphId;
    }
 
    @Override
    public boolean equals(Object object) {
        if (object instanceof CentralityMapId) {
        	CentralityMapId pk = (CentralityMapId)object;
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
