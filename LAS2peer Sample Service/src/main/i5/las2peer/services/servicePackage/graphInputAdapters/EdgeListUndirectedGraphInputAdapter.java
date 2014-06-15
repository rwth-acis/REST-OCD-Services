package i5.las2peer.services.servicePackage.graphInputAdapters;

import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import y.base.Edge;
import y.base.Node;

/**
 * A graph input adapter for edge list .txt files of undirected graphs.
 * 
 * @author Sebastian
 * 
 */
public class EdgeListUndirectedGraphInputAdapter extends AbstractGraphInputAdapter {

	/**
	 * Creates a new instance of the adapter. This constructor is protected and only
	 * to be used by the AdapterFactory.
	 * @param filename
	 *            The name of the .txt file containing the graph.
	 */
	protected EdgeListUndirectedGraphInputAdapter(String filename) {
		this.filename = filename;
	}

	@Override
	public CustomGraph readGraph() {
		CustomGraph graph = new CustomGraph();
		Reader reader = null;
		try {
			reader = new FileReader(filename);
			Map<String, Node> reverseNodeNames = new HashMap<String, Node>();
			while (reader.ready()) {
				String sourceNodeName = readString(reader);
				Node sourceNode;
				if (!reverseNodeNames.containsKey(sourceNodeName)) {
					sourceNode = graph.createNode();
					reverseNodeNames.put(sourceNodeName, sourceNode);
					graph.setNodeName(sourceNode, sourceNodeName);
				}
				else {
					sourceNode = reverseNodeNames.get(sourceNodeName);
				}
				String targetNodeName = readString(reader);
				Node targetNode;
				if (!reverseNodeNames.containsKey(targetNodeName)) {
					targetNode = graph.createNode();
					reverseNodeNames.put(targetNodeName, targetNode);
					graph.setNodeName(targetNode, targetNodeName);
				}
				else {
					targetNode = reverseNodeNames.get(targetNodeName);
				}
				String edgeWeightString = readString(reader);
				double edgeWeight = Double.parseDouble(edgeWeightString);
				Edge edge = graph.createEdge(sourceNode, targetNode);
				Edge reverseEdge = graph.createEdge(targetNode, sourceNode);
				graph.setEdgeWeight(edge, edgeWeight);
				graph.setEdgeWeight(reverseEdge, edgeWeight);
			}
		}
		catch (Exception e) {
			graph = new CustomGraph();
		}
		finally {
			try {
				reader.close();
			}
			catch (Exception e) {
			}
		}
		return graph;
	}

	/*
	 * Reads the next string from the file
	 */
	private String readString(Reader reader) throws IOException {
		String str = "";
		int nextChar = reader.read();
		/*
		 * Skips white space
		 */
		while(Character.isWhitespace(nextChar)) {
			nextChar = reader.read();
		}
		/*
		 * Reads string until next whitespace or EOF
		 */
		while (nextChar != -1 && !Character.isWhitespace(nextChar)) {
			str += (char)nextChar;
			nextChar = reader.read();
		}
		return str;
	}

}
