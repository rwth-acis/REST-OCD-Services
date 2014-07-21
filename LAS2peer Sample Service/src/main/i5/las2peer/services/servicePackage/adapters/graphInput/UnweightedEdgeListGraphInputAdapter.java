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
public class UnweightedEdgeListGraphInputAdapter extends
		AbstractGraphInputAdapter {

	/**
	 * Creates a new instance of the adapter. This constructor is protected and
	 * only to be used by the AdapterFactory.
	 * 
	 * @param filename
	 *            The name of the .txt file containing the graph.
	 */
	public UnweightedEdgeListGraphInputAdapter(String filename) {
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
			 * Reads edges
			 */
			while (line.size() == 2) {
				String sourceNodeName = line.get(0);
				Node sourceNode;
				if (!reverseNodeNames.containsKey(sourceNodeName)) {
					sourceNode = graph.createNode();
					reverseNodeNames.put(sourceNodeName, sourceNode);
					graph.setNodeName(sourceNode, sourceNodeName);
				} else {
					sourceNode = reverseNodeNames.get(sourceNodeName);
				}
				String targetNodeName = line.get(1);
				Node targetNode;
				if (!reverseNodeNames.containsKey(targetNodeName)) {
					targetNode = graph.createNode();
					reverseNodeNames.put(targetNodeName, targetNode);
					graph.setNodeName(targetNode, targetNodeName);
				} else {
					targetNode = reverseNodeNames.get(targetNodeName);
				}
				Edge edge = graph.createEdge(sourceNode, targetNode);
				graph.setEdgeWeight(edge, 1);
				line = Adapters.readLine(reader);
			}
			return graph;
		} catch (Exception e) {
			throw new AdapterException();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
	}

}
