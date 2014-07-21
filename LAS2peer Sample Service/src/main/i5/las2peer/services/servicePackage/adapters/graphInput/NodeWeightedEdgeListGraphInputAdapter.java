package i5.las2peer.services.servicePackage.adapters.graphInput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.Adapters;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import y.base.Edge;
import y.base.Node;

/**
 * A graph input adapter for edge list .txt files.
 * 
 * @author Sebastian
 * 
 */
public class NodeWeightedEdgeListGraphInputAdapter extends AbstractGraphInputAdapter {

	/**
	 * Creates a new instance of the adapter. This constructor is protected and only
	 * to be used by the AdapterFactory.
	 * @param filename
	 *            The name of the .txt file containing the graph.
	 */
	public NodeWeightedEdgeListGraphInputAdapter(String filename) {
		this.filename = filename;
	}

	@Override
	public CustomGraph readGraph() throws AdapterException {
		CustomGraph graph = new CustomGraph();
		Reader reader = null;
		try {
			reader = new FileReader(filename);
			Map<String, Node> reverseNodeNames = new HashMap<String, Node>();
			List<String> line = Adapters.readLine(reader);
			/*
			 * Reads nodes
			 */
			while(line.size() == 1) {
				Node node = graph.createNode();
				String nodeName = line.get(0);
				graph.setNodeName(node, nodeName);
				reverseNodeNames.put(nodeName, node);
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
				if(targetNode == null || sourceNode == null) {
					throw new IllegalArgumentException("Node not specified");
				}
				Edge edge = graph.createEdge(sourceNode, targetNode);
				graph.setEdgeWeight(edge, edgeWeight);
				line = Adapters.readLine(reader);
			}
			return graph;
		}
		catch (Exception e) {
			throw new AdapterException();
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
