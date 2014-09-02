package i5.las2peer.services.ocd.graph;

public class CustomEdgeId {

    private int id;
    
    private CustomGraphId graph;
 
    public CustomEdgeId(int id, CustomGraphId graph) {
        this.id = id;
        this.graph = graph;
    }
 
    public boolean equals(Object object) {
        if (object instanceof CustomEdgeId) {
        	CustomEdgeId pk = (CustomEdgeId)object;
            return graph.equals(graph) && id == pk.id;
        } else {
            return false;
        }
    }
 
    public int hashCode() {
        return (int)(id + graph.hashCode());
    }
	
}
