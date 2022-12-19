package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.CoverId;

/**
 * Composite persistence id of a metric log.
 * @author Sebastian
 *
 */
public class OcdMetricLogId {

	/**
	 * The specific log key.
	 */
    private String key;
    
    /**
     * The id of the corresponding cover.
     */
    private CoverId cover;
 
    /**
     * Creates a new instance.
     * @param key The log key.
     * @param coverId The id of the corresponding cover.
     */
    public OcdMetricLogId(String key, CoverId coverId) {
        this.key = key;
        this.cover = coverId;
    }
 
    @Override
    public boolean equals(Object object) {
        if (object instanceof OcdMetricLogId) {
        	OcdMetricLogId pk = (OcdMetricLogId)object;
            return cover.equals(cover) && key.equals(pk.key);
        } else {
            return false;
        }
    }
 
    @Override
    public int hashCode() {
        return (int)(key.hashCode() + cover.hashCode());
    }
    
    /**
     * Returns the id of the corresponding cover.
     * @return The id.
     */
    public CoverId getCoverId() {
    	return cover;
    }
    
    /**
     * Returns the specific log key.
     * @return The id.
     */
    public String getKey() {
    	return key;
    }
	
}
