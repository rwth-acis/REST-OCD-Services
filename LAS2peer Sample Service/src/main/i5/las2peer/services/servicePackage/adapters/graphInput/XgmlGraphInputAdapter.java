package i5.las2peer.services.servicePackage.adapters.graphInput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.util.HashMap;
import java.util.Map;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.io.IOHandler;
import y.io.XGMLIOHandler;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeRealizer;

/**
 * A graph input adapter for XGML files.
 * @author Sebastian
 *
 */
public class XgmlGraphInputAdapter extends AbstractGraphInputAdapter {
	
	/**
	 * Creates a new instance of the adapter. This constructor is protected and only
	 * to be used by the AdapterFactory.
	 * @param filename The name of the XGML file containing the graph.
	 */
	/*
	 * Protected constructor to prevent instantiation by anything but the AdapterFactory
	 */
	public XgmlGraphInputAdapter(String filename) {
		this.filename = filename;
	}
	
	@Override
	public CustomGraph readGraph() throws AdapterException {
		try {  
		    // Reads in the graph using an IOHandler.
			Graph2D inputGraph = new Graph2D();
			IOHandler ioh = new XGMLIOHandler();
		    ioh.read(inputGraph, filename);
		    // Sets the node names according to the first label of each node
		    Map<Node, String> nodeNames = new HashMap<Node, String>();
		    NodeCursor nodes = inputGraph.nodes();
		    while(nodes.ok()) {
		    	Node node = nodes.node();
		    	NodeRealizer nRealizer = inputGraph.getRealizer(node);
		    	String nodeName = nRealizer.getLabelText();
		    	nodeNames.put(node, nodeName);
		    	nodes.next();
		    }
		    // Sets the edge weights according to the first label of each edge
		    Map<Edge, Double> edgeWeights = new HashMap<Edge, Double>();
		    EdgeCursor edges = inputGraph.edges();
		    while(edges.ok()) {
		    	Edge edge = edges.edge();
		    	EdgeRealizer eRealizer = inputGraph.getRealizer(edge);
		    	double edgeWeight = Double.parseDouble(eRealizer.getLabelText());
		    	edgeWeights.put(edge, edgeWeight);
		    	edges.next();
		    }
		    return new CustomGraph(inputGraph, edgeWeights, nodeNames);
		}  
		catch (Exception e) {
			throw new AdapterException();
		}
	}
}
