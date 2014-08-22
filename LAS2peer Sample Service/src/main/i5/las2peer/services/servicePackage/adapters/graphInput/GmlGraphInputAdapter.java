package i5.las2peer.services.servicePackage.adapters.graphInput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import y.base.Node;
import y.base.NodeCursor;
import y.io.GMLIOHandler;
import y.io.IOHandler;

public class GmlGraphInputAdapter extends AbstractGraphInputAdapter {
	
	@Override
	public CustomGraph readGraph() throws AdapterException {
		IOHandler ioh = new GMLIOHandler();
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
		NodeCursor nodes = graph.nodes();
		Node node;
		while(nodes.ok()) {
			node = nodes.node();
			graph.setNodeName(node, Integer.toString(node.index()));
			nodes.next();
		}
		return graph;
	}

}
