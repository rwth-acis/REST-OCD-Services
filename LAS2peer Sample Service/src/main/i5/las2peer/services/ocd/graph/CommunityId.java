package i5.las2peer.services.ocd.graph;

public class CommunityId {

    private long id;
    
    private CoverId cover;
 
    public CommunityId(int id, CoverId cover) {
        this.id = id;
        this.cover = cover;
    }
 
    public boolean equals(Object object) {
        if (object instanceof CommunityId) {
        	CommunityId pk = (CommunityId)object;
            return cover.equals(cover) && id == pk.id;
        } else {
            return false;
        }
    }
 
    public int hashCode() {
        return (int)(id + cover.hashCode());
    }
	
}
