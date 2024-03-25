package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.Adapters;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.MultiplexGraph;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.io.Reader;
import java.text.ParseException;
import java.util.*;

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
		CustomGraph representativeGraph = new CustomGraph();
		try {
			Map<String, Map<String, Node>> graphReverseNodeNames = new HashMap<String, Map<String, Node>>();
			HashMap<String, Node> representativeGraphReverseNodeNames = new HashMap<String, Node>();
			List<String> line = Adapters.readLine(reader);
			Set<String> totalNumberOfNodes = new HashSet<String>();
			int totalNumberOfEdges = 0;
			/*
			 * Reads edges
			 */
			while(line.size() == 4) {
				//read layer
				String layerName = line.get(0);
				if (!multiplexGraph.getCustomGraphs().containsKey(layerName)) {
					multiplexGraph.addLayer(layerName, new CustomGraph());
					graphReverseNodeNames.put(layerName, new HashMap<String, Node>());
				}
				CustomGraph graph = multiplexGraph.getCustomGraphs().get(layerName);
				graph.setName(layerName);
				Map<String, Node>reverseNodeNames = graphReverseNodeNames.get(layerName);

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
				String targetNodeName = line.get(2);
				Node targetNode;
				if (!reverseNodeNames.containsKey(targetNodeName)) {
					targetNode = graph.addNode(targetNodeName);
					reverseNodeNames.put(targetNodeName, targetNode);
					graph.setNodeName(targetNode, targetNodeName);
				}
				else {
					targetNode = reverseNodeNames.get(targetNodeName);
				}
				String edgeWeightString = line.get(3);
				double edgeWeight = Double.parseDouble(edgeWeightString);
				Edge edge = graph.addEdge(UUID.randomUUID().toString(), sourceNode, targetNode);
				graph.setEdgeWeight(edge, edgeWeight);

				//add nodes and edge to representative graph
				if (!representativeGraphReverseNodeNames.containsKey(sourceNodeName)) {
					sourceNode = representativeGraph.addNode(sourceNodeName);
					representativeGraphReverseNodeNames.put(sourceNodeName, sourceNode);
					representativeGraph.setNodeName(sourceNode, sourceNodeName);
				} else {
					sourceNode = representativeGraphReverseNodeNames.get(sourceNodeName);
				}
				if (!representativeGraphReverseNodeNames.containsKey(targetNodeName)) {
					targetNode = representativeGraph.addNode(targetNodeName);
					representativeGraphReverseNodeNames.put(targetNodeName, targetNode);
					representativeGraph.setNodeName(targetNode, targetNodeName);
				} else {
					targetNode = representativeGraphReverseNodeNames.get(targetNodeName);
				}
				representativeGraph.addEdge(UUID.randomUUID().toString(), sourceNode, targetNode);

				line = Adapters.readLine(reader);

				totalNumberOfEdges++;
				totalNumberOfNodes.add(sourceNodeName);
				totalNumberOfNodes.add(targetNodeName);
			}
			if(line.size() > 0) {
				throw new AdapterException("Invalid input format");
			}
			//make sure all nodes appear on all layers/in all CustomGraphs
			for (CustomGraph graph : multiplexGraph.getCustomGraphs().values()) {
				String layerName = graph.getName();
				Map<String, Node>reverseNodeNames = graphReverseNodeNames.get(layerName);
				for(String nodeName: totalNumberOfNodes) {
					if (!reverseNodeNames.containsKey(nodeName)) {
						Node node = graph.addNode(nodeName);
						graph.setNodeName(node, nodeName);
					}
				}
			}
			multiplexGraph.setRepresentativeGraph(representativeGraph);

			multiplexGraph.setNodeCount(totalNumberOfNodes.size());
			multiplexGraph.setEdgeCount(totalNumberOfEdges);
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
