package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.Adapters;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import y.base.Edge;
import y.base.Node;

/**
 * A graph input adapter for node weighted edge list format.
 * There are two sections, immediately following one another without any special indications.
 * In the first section each line of input contains only a node name. There must be one line for each node.
 * In the second section each line of input contains first the name of a source node of an edge, then the name of a target node of an edge
 * and finally a double value as the edge weight, using the space character (' ') as a delimiter. There must be one line for each edge.
 * @author Sebastian
 *
 */
public class NodeWeightedEdgeListGraphInputAdapter extends AbstractGraphInputAdapter {

	/**
	 * Creates a new instance setting the reader attribute.
	 * @param reader The reader to receive input from.
	 */
	public NodeWeightedEdgeListGraphInputAdapter(Reader reader) {
		this.setReader(reader);
	}
	
	/**
	 * Creates a new instance.
	 */
	public NodeWeightedEdgeListGraphInputAdapter() {
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
			if(line.size() > 0) {
				throw new AdapterException("Invalid input format");
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
