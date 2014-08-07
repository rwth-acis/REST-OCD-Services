package i5.las2peer.services.servicePackage.adapters.graphInput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.Adapters;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import y.base.Edge;
import y.base.Node;

/**
 * A graph input adapter for the node list and weighted edge list format.
 * 
 * @author Sebastian
 * 
 */
public class NodeWeightedEdgeListGraphInputAdapter extends AbstractGraphInputAdapter {

	/**
	 * Creates a new instance of the adapter.
	 * @param reader The reader the graph will be read from.
	 */
	public NodeWeightedEdgeListGraphInputAdapter(Reader reader) {
		this.setReader(reader);
	}

	@Override
	public CustomGraph readGraph() throws AdapterException {
		CustomGraph graph = new CustomGraph();
		try {
			Map<String, Node> reverseNodeNames = new HashMap<String, Node>();
			List<String> line = Adapters.readLine(reader);
			/*
			 * Reads nodes
			 */
			while(line.size() == 1) {
				Node node = graph.createNode();
				String nodeName = line.get(0);
				if(!reverseNodeNames.containsKey(nodeName)) {
					graph.setNodeName(node, nodeName);
					reverseNodeNames.put(nodeName, node);
				}
				else {
					throw new AdapterException("Node name not unique: " + nodeName);
				}
				line = Adapters.readLine(reader);
			}
			/*
			 * Reads edges
			 */
			while(line.size() == 3) {
				String sourceNodeName = line.get(0);
				Node sourceNode = reverseNodeNames.get(sourceNodeName);
				String targetNodeName = line.get(1);
				Node targetNode = reverseNodeNames.get(targetNodeName);
				double edgeWeight = Double.parseDouble(line.get(2));
				if(sourceNode == null) {
					throw new AdapterException("Node not specified: " + sourceNodeName);
				}
				if(targetNode == null) {
					throw new AdapterException("Node not specified: " + targetNodeName);
				}
				Edge edge = graph.createEdge(sourceNode, targetNode);
				graph.setEdgeWeight(edge, edgeWeight);
				line = Adapters.readLine(reader);
			}
			return graph;
		}
		catch (Exception e) {
			throw new AdapterException(e);
		}
		finally {
			try {
				reader.close();
			}
			catch (Exception e) {
			}
		}
	}
	
}
