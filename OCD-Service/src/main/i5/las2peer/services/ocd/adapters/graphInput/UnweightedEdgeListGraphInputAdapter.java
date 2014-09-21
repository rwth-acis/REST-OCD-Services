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
 * A graph input adapter for unweighted edge list format.
 * Each line of input contains first the name of a source node of an edge, then the name of a target node of an edge,
 * using the space character (' ') as a delimiter. There must be one line for each edge.
 * @author Sebastian
 *
 */
public class UnweightedEdgeListGraphInputAdapter extends
		AbstractGraphInputAdapter {

	/**
	 * Creates a new instance setting the reader attribute.
	 * @param reader The reader to receive input from.
	 */
	public UnweightedEdgeListGraphInputAdapter(Reader reader) {
		this.setReader(reader);
	}
	
	/**
	 * Creates a new instance.
	 */
	public UnweightedEdgeListGraphInputAdapter() {
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
			if(line.size() > 0) {
				throw new AdapterException("Invalid input format");
			}
			return graph;
		} catch (Exception e) {
			throw new AdapterException(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
	}

}
