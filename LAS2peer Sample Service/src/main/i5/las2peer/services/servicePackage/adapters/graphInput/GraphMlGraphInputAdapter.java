package i5.las2peer.services.servicePackage.adapters.graphInput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

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
			NodeCursor nodes = graph.nodes();
			Node node;
			while(nodes.ok()) {
				node = nodes.node();
				graph.setNodeName(node, (String)nodeNames.get(node));
				nodes.next();
			}
			EdgeCursor edges = graph.edges();
			Edge edge;
			while(edges.ok()) {
				edge = edges.edge();
				graph.setEdgeWeight(edge, (Double)edgeWeights.get(edge));
				edges.next();
			}
		} catch (IOException e) {
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
