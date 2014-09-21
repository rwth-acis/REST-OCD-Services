package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeMap;
import y.io.GraphMLIOHandler;
import y.io.graphml.GraphMLHandler;
import y.io.graphml.KeyScope;
import y.io.graphml.KeyType;

/**
 * A graph input adapter for GraphML format, based on the GraphMLIOHandler of the yFiles library..
 * In case each node has a "name" element containing a CDATA section with a unique value node names will be derived from there. Otherwise node names will be set as indices.
 * In case each edge has a "weight" element with a numeric value edge weights will be derived from there.
 * @author Sebastian
 *
 */
public class GraphMlGraphInputAdapter extends AbstractGraphInputAdapter {

	@Override
	public CustomGraph readGraph() throws AdapterException {
		Scanner scanner = new Scanner(reader);
		String inString = scanner.useDelimiter("\\A").next();
		scanner.close();
		InputStream is = new ByteArrayInputStream(inString.getBytes());
		GraphMLIOHandler ioh = new GraphMLIOHandler();
		CustomGraph graph = new CustomGraph();
		NodeMap nodeNames = graph.createNodeMap();
		EdgeMap edgeWeights = graph.createEdgeMap();
		GraphMLHandler core = ioh.getGraphMLHandler();
		core.addInputDataAcceptor("name", nodeNames, KeyScope.NODE, KeyType.STRING);
		core.addInputDataAcceptor("weight", edgeWeights, KeyScope.EDGE, KeyType.DOUBLE);
		try {
			ioh.read(graph, is);
			/*
			 * Checks whether node names are unique.
			 */
			NodeCursor nodes = graph.nodes();
			Node node;
			String name;
			Set<String> names = new HashSet<String>();
			while(nodes.ok()) {
				name = (String)nodeNames.get(nodes.node());
				if(name.isEmpty()) {
					break;
				}
				names.add(name);
				nodes.next();
			}
			nodes.toFirst();
			/*
			 * Sets unique node names.
			 */
			if(names.size() == graph.nodeCount()) {
				while(nodes.ok()) {
					node = nodes.node();
					graph.setNodeName(node, (String)nodeNames.get(node));
					nodes.next();
				}
			}
			/*
			 * If names not unique sets indices instead.
			 */
			else {
				while(nodes.ok()) {
					node = nodes.node();
					graph.setNodeName(node, Integer.toString(node.index()));
					nodes.next();
				}
			}
			EdgeCursor edges = graph.edges();
			Edge edge;
			while(edges.ok()) {
				edge = edges.edge();
				Double weight = (Double)edgeWeights.get(edge);
				if(weight != null) {
					graph.setEdgeWeight(edge, weight);
				}
				edges.next();
			}
		} catch (Exception e) {
			throw new AdapterException(e);
		} finally {
			graph.disposeNodeMap(nodeNames);
			graph.disposeEdgeMap(edgeWeights);
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return graph;
	}

}
