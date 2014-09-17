package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.CoverId;

public class OcdMetricLogId {

    private long id;
    
    private CoverId cover;
 
    public OcdMetricLogId(long id, CoverId cover) {
        this.id = id;
        this.cover = cover;
    }
 
    public boolean equals(Object object) {
        if (object instanceof OcdMetricLogId) {
        	OcdMetricLogId pk = (OcdMetricLogId)object;
            return cover.equals(cover) && id == pk.id;
        } else {
            return false;
        }
    }
 
    public int hashCode() {
        return (int)(id + cover.hashCode());
    }
    
    public CoverId getCoverId() {
    	return cover;
    }
    
    public long getId() {
    	return id;
    }
	
}
