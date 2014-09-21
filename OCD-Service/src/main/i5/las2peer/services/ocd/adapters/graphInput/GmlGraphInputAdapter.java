package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.io.GMLIOHandler;

/**
 * A graph input adapter for GML format, based on the GMLIOHandler of the yFiles library.
 * In case each node has a label with a unique value node names will be derived from there. Otherwise node names will be set as indices.
 * In case each edge has a label with a numeric value edge weights will be derived from there.
 * @author Sebastian
 *
 */
public class GmlGraphInputAdapter extends AbstractGraphInputAdapter {
	
	@Override
	public CustomGraph readGraph() throws AdapterException {
		GMLIOHandler ioh = new GMLIOHandler();
		Scanner scanner = new Scanner(reader);
		String inString = scanner.useDelimiter("\\A").next();
		scanner.close();
		InputStream is = new ByteArrayInputStream(inString.getBytes());
		CustomGraph graph = new CustomGraph();
		try {
			ioh.read(graph, is);
		} catch (IOException e) {
			throw new AdapterException(e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		/*
		 * Check whether node labels are unique names.
		 */
		NodeCursor nodes = graph.nodes();
		Node node;
		HashMap<Node, String> nodenames = new HashMap<Node, String>();
		try {
			while(nodes.ok()) {
				node = nodes.node();
				String name = graph.getRealizer(node).getLabel().getText();
				if(name.isEmpty()) {
					break;
				}
				nodenames.put(node, name);
				nodes.next();
			}
		} catch (RuntimeException e) {
			// label not set
		}
		nodes.toFirst();
		/*
		 * Node labels are unique.
		 */
		if(nodenames.values().size() == graph.nodeCount()) {
			while(nodes.ok()) {
				node = nodes.node();
				graph.setNodeName(node, nodenames.get(node));
				nodes.next();
			}
		}
		/*
		 * Node labels are not unique.
		 */
		else {
			while(nodes.ok()) {
				node = nodes.node();
				graph.setNodeName(node, nodenames.get(node));
				nodes.next();
			}
		}
		/*
		 * Check whether node labels are numeric.
		 */
		EdgeCursor edges = graph.edges();
		Edge edge;
		HashMap<Edge, Double> edgeweights = new HashMap<Edge, Double>();
		try {
			while(edges.ok()) {
				edge = edges.edge();
				String weightStr = graph.getRealizer(edge).getLabel().getText();
				Double weight = Double.parseDouble(weightStr);
				if(weight != null) {
					edgeweights.put(edge, weight);
				}
				edges.next();
			}
		} catch (RuntimeException e) {
			// label not set
		}
		edges.toFirst();
		/*
		 * all labels correspond numeric
		 */
		if(edgeweights.size() == graph.edgeCount()) {
			while(edges.ok()) {
				edge = edges.edge();
				graph.setEdgeWeight(edge, edgeweights.get(edge));
				edges.next();
			}
		}
		return graph;
	}

}
