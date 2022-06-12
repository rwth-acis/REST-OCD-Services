package i5.las2peer.services.ocd.graphs;

/**
 * Composite persistence id of a graph.
 * @author Sebastian
 *
 */
public class CustomGraphId {
	
	/**
	 * The graph-specific id.
	 */
    private String id;
    
    /**
     * The name of the user owning the graph.
     */
    private String userName;
 
    /**
     * Creates a new instance.
     * @param id The graph-specific id.
     * @param userName The name of the user owning the graph.
     */
    public CustomGraphId(String id, String userName) {
        this.id = id;
        this.userName = userName;
    }
 
    @Override
    public boolean equals(Object object) {
        if (object instanceof CustomGraphId) {
        	CustomGraphId pk = (CustomGraphId)object;
            return userName.equals(pk.userName) && id == pk.id;
        } else {
            return false;
        }
    }
 
    @Override
    public int hashCode() {
        return (id.hashCode() + userName.hashCode());
    }
	
}
