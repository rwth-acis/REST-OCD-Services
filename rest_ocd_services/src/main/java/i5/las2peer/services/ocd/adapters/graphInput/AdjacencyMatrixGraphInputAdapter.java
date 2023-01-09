package i5.las2peer.services.ocd.adapters.graphInput;

import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.Adapters;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

/**
 * A graph input adapter for adjacency matrix format.
 *
 */
public class AdjacencyMatrixGraphInputAdapter extends AbstractGraphInputAdapter {
	
	/**
	 * Creates a new instance setting the reader attribute.
	 * @param reader The reader to receive input from.
	 */
	public AdjacencyMatrixGraphInputAdapter(Reader reader) {
		this.setReader(reader);
	}
	
	public AdjacencyMatrixGraphInputAdapter() {

	}
	
	@Override
	public CustomGraph readGraph() throws AdapterException {

		CustomGraph graph = new CustomGraph();
		try {
			List<String> line = Adapters.readLine(reader);
			int nodeCount = line.size();
			List<Node> nodes = new ArrayList<>();
			for (int i = 0; i < nodeCount; i++) {
				nodes.add(i, graph.addNode(String.valueOf(i)));
				graph.setNodeName(nodes.get(i), String.valueOf(i+1));
			}

			for (int row = 0; row < nodeCount; row++) {
				Node sourceNode = nodes.get(row);
				for (int column = 0; column < nodeCount; column++) {
					Node targetNode = nodes.get(column);
					double edgeWeight = Double.parseDouble(line.get(column));
					if (edgeWeight > 0) {
						Edge edge = graph.addEdge(UUID.randomUUID().toString(), sourceNode, targetNode);
						graph.setEdgeWeight(edge, edgeWeight);
					}
				}
				line = Adapters.readLine(reader);
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

	@Override
	public void setParameter(Map<String, String> param) throws IllegalArgumentException, ParseException {

	}

}
