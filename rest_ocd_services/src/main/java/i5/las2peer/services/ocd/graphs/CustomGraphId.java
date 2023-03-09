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
    private String key;
    
    /**
     * The name of the user owning the graph.
     */
    private String userName;
 
    /**
     * Creates a new instance.
     * @param key The graph-specific key.
     * @param userName The name of the user owning the graph.
     */
    public CustomGraphId(String key, String userName) {
        this.key = key;
        this.userName = userName;
    }
 
    @Override
    public boolean equals(Object object) {
        if (object instanceof CustomGraphId pk) {
            return userName.equals(pk.userName) && key.equals(pk.key);
        } else {
            return false;
        }
    }
 
    @Override
    public int hashCode() {
        return (int)(key.hashCode() + userName.hashCode());
    }
    public String getUser() {
    	return userName;
    }
    public String getKey() {
    	return key;
    }
	
}
