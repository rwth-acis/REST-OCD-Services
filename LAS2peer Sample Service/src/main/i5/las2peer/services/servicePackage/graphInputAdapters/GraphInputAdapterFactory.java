package i5.las2peer.services.servicePackage.graphInputAdapters;


/**
 *	A singleton factory for producing concrete
 * instances of the adapters.
 *
 * @author Sebastian
 *
 */
public class GraphInputAdapterFactory {
	
	/*
	 * The unique factory instance
	 */
    static private GraphInputAdapterFactory adapterFactory;
 
    private GraphInputAdapterFactory() {
    }    
 
    /**
     * The getter for the unique factory instance.
     */
    public static GraphInputAdapterFactory getFactory() {
		if (adapterFactory == null) {
			if (adapterFactory == null) {
				adapterFactory = new GraphInputAdapterFactory();
		    }
		}
		return adapterFactory;
    }
    
    /**
     * Returns an instance of the XGML Adapter
     */
    public GraphInputAdapter getXgmlGraphInputAdapter(String filename) {
    	return new XgmlGraphInputAdapter(filename);
    }
    
    public GraphInputAdapter getEdgeListGraphInputAdapter(String filename) {
    	return new EdgeListGraphInputAdapter(filename);
    }
    
    public GraphInputAdapter getEdgeListUndirectedGraphInputAdapter(String filename) {
    	return new EdgeListUndirectedGraphInputAdapter(filename);
    }
}
