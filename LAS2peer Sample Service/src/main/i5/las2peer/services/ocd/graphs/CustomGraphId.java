package i5.las2peer.services.ocd.graphs;

public class CustomGraphId {
	
    private long id;
    
    private String userName;
 
    public CustomGraphId(long id, String userName) {
        this.id = id;
        this.userName = userName;
    }
 
    public boolean equals(Object object) {
        if (object instanceof CustomGraphId) {
        	CustomGraphId pk = (CustomGraphId)object;
            return userName.equals(pk.userName) && id == pk.id;
        } else {
            return false;
        }
    }
 
    public int hashCode() {
        return (int)(id + userName.hashCode());
    }
	
}
