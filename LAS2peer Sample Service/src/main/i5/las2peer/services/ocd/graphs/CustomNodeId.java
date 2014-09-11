package i5.las2peer.services.ocd.graphs;

public class CustomNodeId {

    private int id;
    
    private CustomGraphId graph;
 
    public CustomNodeId(int id, CustomGraphId graph) {
        this.id = id;
        this.graph = graph;
    }
 
    public boolean equals(Object object) {
        if (object instanceof CustomNodeId) {
        	CustomNodeId pk = (CustomNodeId)object;
            return graph.equals(graph) && id == pk.id;
        } else {
            return false;
        }
    }
 
    public int hashCode() {
        return (int)(id + graph.hashCode());
    }
	
}
