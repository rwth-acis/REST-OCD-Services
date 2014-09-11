package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.CoverId;

public class MetricLogId {

    private long id;
    
    private CoverId cover;
 
    public MetricLogId(int id, CoverId cover) {
        this.id = id;
        this.cover = cover;
    }
 
    public boolean equals(Object object) {
        if (object instanceof MetricLogId) {
        	MetricLogId pk = (MetricLogId)object;
            return cover.equals(cover) && id == pk.id;
        } else {
            return false;
        }
    }
 
    public int hashCode() {
        return (int)(id + cover.hashCode());
    }
	
}
