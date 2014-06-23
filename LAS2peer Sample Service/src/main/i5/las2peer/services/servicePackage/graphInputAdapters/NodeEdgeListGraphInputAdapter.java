package i5.las2peer.services.servicePackage.graphInputAdapters;

import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import y.base.Edge;
import y.base.Node;

/**
 * A graph input adapter for edge list .txt files.
 * 
 * @author Sebastian
 * 
 */
public class NodeEdgeListGraphInputAdapter extends AbstractGraphInputAdapter {

	/**
	 * Creates a new instance of the adapter. This constructor is protected and only
	 * to be used by the AdapterFactory.
	 * @param filename
	 *            The name of the .txt file containing the graph.
	 */
	public NodeEdgeListGraphInputAdapter(String filename) {
		this.filename = filename;
	}

	@Override
	public CustomGraph readGraph() {
		CustomGraph graph = new CustomGraph();
		Reader reader = null;
		try {
			reader = new FileReader(filename);
			Map<String, Node> reverseNodeNames = new HashMap<String, Node>();
			List<String> line = readLine(reader);
			/*
			 * Reads nodes
			 */
			while(line.size() == 1) {
				Node node = graph.createNode();
				String nodeName = line.get(0);
				graph.setNodeName(node, nodeName);
				reverseNodeNames.put(nodeName, node);
				line = readLine(reader);
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
				if(targetNode == null || sourceNode == null) {
					throw new IllegalArgumentException("Node not specified");
				}
				Edge edge = graph.createEdge(sourceNode, targetNode);
				graph.setEdgeWeight(edge, edgeWeight);
				line = readLine(reader);
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
	 * Reads in the next line and returns it as a list of strings.
	 * Strings are separated according to white space.
	 * @param reader - The reader from which the next line is read.
	 * @return A list of all strings in the line.
	 * @throws IOException
	 */
	private List<String> readLine(Reader reader) throws IOException {
		List<String> line = new ArrayList<String>();
		int nextChar = reader.read();
		/*
		 * Skips potential additional line break characters.
		 */
		while(Character.getType(nextChar) == Character.CONTROL) {
			nextChar = reader.read();
		}
		/*
		 * Extracts all strings from the line, separated by whitespace
		 */
		while(Character.getType(nextChar) != Character.CONTROL && reader.ready()) {
			String str = "";		
			/*
			 * Skips white space other than line separators
			 */
			while(Character.isWhitespace(nextChar) && Character.getType(nextChar) != Character.CONTROL) {
				nextChar = reader.read();
			}
			/*
			 * Reads string until next whitespace or EOF
			 */
			while (nextChar != -1 && !Character.isWhitespace(nextChar) && Character.getType(nextChar) != Character.CONTROL) {
				str += (char)nextChar;
				nextChar = reader.read();
			}
			if(!str.equals("")) {
				line.add(str);
			}
		}
		return line;
	}
	
}
