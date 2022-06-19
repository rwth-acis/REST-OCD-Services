package i5.las2peer.services.ocd.graphs;

/**
 * Composite persistence id of a graph.
 * @author Sebastian
 *
 */
public class CustomGraphId {
	
	/**
	 * The graph-specific persistence id.
	 */
    private long persistenceId;
    
    /**
     * The name of the user owning the graph.
     */
    private String userName;
 
    /**
     * Creates a new instance.
     * @param id The graph-specific id.
     * @param userName The name of the user owning the graph.
     */
    public CustomGraphId(long id, String userName) {
        this.persistenceId = id;
        this.userName = userName;
    }
 
    @Override
    public boolean equals(Object object) {
        if (object instanceof CustomGraphId) {
        	CustomGraphId pk = (CustomGraphId)object;
            return userName.equals(pk.userName) && persistenceId == pk.persistenceId;
        } else {
            return false;
        }
    }
 
    @Override
    public int hashCode() {
        return (int)(persistenceId + userName.hashCode());
    }
	
}
