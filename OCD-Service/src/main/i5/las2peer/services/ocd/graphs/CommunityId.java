package i5.las2peer.services.ocd.graphs;

/**
 * Composite persistence id of a community.
 * @author Sebastian
 *
 */
public class CommunityId {

	/**
	 * The community-specific id.
	 */
    private long id;
    
    /**
     * The id of the cover the community is part of.
     */
    private CoverId cover;
 
    /**
     * Creates a new instance.
     * @param id The community-specific id.
     * @param coverId The id of the cover the community is part of.
     */
    public CommunityId(int id, CoverId coverId) {
        this.id = id;
        this.cover = coverId;
    }
 
    @Override
    public boolean equals(Object object) {
        if (object instanceof CommunityId) {
        	CommunityId pk = (CommunityId)object;
            return cover.equals(cover) && id == pk.id;
        } else {
            return false;
        }
    }
 
    @Override
    public int hashCode() {
        return (int)(id + cover.hashCode());
    }
	
}
