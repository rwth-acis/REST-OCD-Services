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
 * A graph input adapter for the weighted edge list format.
 * 
 * @author Sebastian
 * 
 */
public class WeightedEdgeListGraphInputAdapter extends AbstractGraphInputAdapter {

	/**
	 * Creates a new instance of the adapter.
	 * @param reader The reader the graph will be read from.
	 */
	public WeightedEdgeListGraphInputAdapter(Reader reader) {
		this.setReader(reader);
	}

	@Override
	public CustomGraph readGraph() throws AdapterException {
		CustomGraph graph = new CustomGraph();
		try {
			Map<String, Node> reverseNodeNames = new HashMap<String, Node>();
			List<String> line = Adapters.readLine(reader);
			/*
			 * Reads edges
			 */
			while(line.size() == 3) {
				String sourceNodeName = line.get(0);
				Node sourceNode;
				if (!reverseNodeNames.containsKey(sourceNodeName)) {
					sourceNode = graph.createNode();
					reverseNodeNames.put(sourceNodeName, sourceNode);
					graph.setNodeName(sourceNode, sourceNodeName);
				}
				else {
					sourceNode = reverseNodeNames.get(sourceNodeName);
				}
				String targetNodeName = line.get(1);
				Node targetNode;
				if (!reverseNodeNames.containsKey(targetNodeName)) {
					targetNode = graph.createNode();
					reverseNodeNames.put(targetNodeName, targetNode);
					graph.setNodeName(targetNode, targetNodeName);
				}
				else {
					targetNode = reverseNodeNames.get(targetNodeName);
				}
				String edgeWeightString = line.get(2);
				double edgeWeight = Double.parseDouble(edgeWeightString);
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
