package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.Adapters;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.MultiplexGraph;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.io.Reader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A graph input adapter for weighted edge list format.
 * Each line of input contains first the name of a source node of an edge, then the name of a target node of an edge
 * and finally a double value as the edge weight, using the space character (' ') as a delimiter. There must be one line for each edge.
 * @author Maren
 *
 */
public class MultiplexWeightedEdgeListGraphInputAdapter extends AbstractMultiplexGraphInputAdapter {

	/**
	 * Creates a new instance setting the reader attribute.
	 * @param reader The reader to receive input from.
	 */
	public MultiplexWeightedEdgeListGraphInputAdapter(Reader reader) {
		this.setReader(reader);
	}

	/**
	 * Creates a new instance.
	 */
	public MultiplexWeightedEdgeListGraphInputAdapter() {
	}
	
	@Override
	public void setParameter(Map<String,String> param) throws IllegalArgumentException, ParseException{
		
	}

	@Override
	public MultiplexGraph readGraph() throws AdapterException {
		MultiplexGraph multiplexGraph = new MultiplexGraph();
		try {
			Map<String, Node> reverseNodeNames = new HashMap<String, Node>();
			List<String> line = Adapters.readLine(reader);
			/*
			 * Reads edges
			 */
			while(line.size() == 4) {
				//read layer
				String layerName = line.get(0);
				if (!multiplexGraph.getCustomGraphs().containsKey(layerName)) {
					multiplexGraph.addLayer(layerName, new CustomGraph());
				}
				CustomGraph graph = multiplexGraph.getCustomGraphs().get(layerName);

				//read edge
				String sourceNodeName = line.get(1);
				Node sourceNode;
				if (!reverseNodeNames.containsKey(sourceNodeName)) {
					sourceNode = graph.addNode(sourceNodeName);
					reverseNodeNames.put(sourceNodeName, sourceNode);
					graph.setNodeName(sourceNode, sourceNodeName);
				}
				else {
					sourceNode = reverseNodeNames.get(sourceNodeName);
				}
				String targetNodeName = line.get(1);
				Node targetNode;
				if (!reverseNodeNames.containsKey(targetNodeName)) {
					targetNode = graph.addNode(targetNodeName);
					reverseNodeNames.put(targetNodeName, targetNode);
					graph.setNodeName(targetNode, targetNodeName);
				}
				else {
					targetNode = reverseNodeNames.get(targetNodeName);
				}
				String edgeWeightString = line.get(2);
				double edgeWeight = Double.parseDouble(edgeWeightString);
				Edge edge = graph.addEdge(UUID.randomUUID().toString(), sourceNode, targetNode);
				graph.setEdgeWeight(edge, edgeWeight);
				line = Adapters.readLine(reader);
			}
			if(line.size() > 0) {
				throw new AdapterException("Invalid input format");
			}
			return multiplexGraph;
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
