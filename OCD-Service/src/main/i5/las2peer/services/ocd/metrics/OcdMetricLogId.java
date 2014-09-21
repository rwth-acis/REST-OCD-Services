package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.CoverId;

/**
 * Composite persistence id of a metric log.
 * @author Sebastian
 *
 */
public class OcdMetricLogId {

	/**
	 * The specific log id.
	 */
    private long id;
    
    /**
     * The id of the corresponding cover.
     */
    private CoverId cover;
 
    /**
     * Creates a new instance.
     * @param id The log id.
     * @param coverId The id of the corresponding cover.
     */
    public OcdMetricLogId(long id, CoverId coverId) {
        this.id = id;
        this.cover = coverId;
    }
 
    @Override
    public boolean equals(Object object) {
        if (object instanceof OcdMetricLogId) {
        	OcdMetricLogId pk = (OcdMetricLogId)object;
            return cover.equals(cover) && id == pk.id;
        } else {
            return false;
        }
    }
 
    @Override
    public int hashCode() {
        return (int)(id + cover.hashCode());
    }
    
    /**
     * Returns the id of the corresponding cover.
     * @return The id.
     */
    public CoverId getCoverId() {
    	return cover;
    }
    
    /**
     * Returns the specific log id.
     * @return The id.
     */
    public long getId() {
    	return id;
    }
	
}
