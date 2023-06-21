package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

/**
 * A graph output adapter for weighted edge list format.
 * Each line of output contains first the name of a source node of an edge, then the name of a target node of an edge
 * and finally a double value as the edge weight (optional), using the space character (' ') as a delimiter. There is one line for each edge.
 * @author Sebastian
 * 
 */
public class WeightedEdgeListGraphOutputAdapter extends AbstractGraphOutputAdapter {

	boolean weighted = true;
	
	/**
	 * Creates a new instance setting the writer attribute.
	 * @param writer The writer used for output.
	 */
	public WeightedEdgeListGraphOutputAdapter(Writer writer) {
		this.setWriter(writer);
	}

	/**
	 * Creates a new instance.
	 */
	public WeightedEdgeListGraphOutputAdapter() {
	}
	
	public boolean isWeighted() {
		return weighted;
	}

	public void setWeighted(boolean weighted) {
		this.weighted = weighted;
	}

	@Override
	public void writeGraph(CustomGraph graph) throws AdapterException {
		try {
			Iterator<Edge> edges = graph.edges().iterator();
			Edge edge;
			while(edges.hasNext()) {
				edge = edges.next();
				HashSet<String> adj_set = new HashSet<>();
				Iterator<Node> adj_it = edge.getSourceNode().neighborNodes().iterator();
				while (adj_it.hasNext()) {
					String next_id = adj_it.next().getId();
					if(!next_id.equals(edge.getSourceNode().getId())) {
						adj_set.add(next_id);
					}
				}

				if(!adj_set.isEmpty()) {
					System.out.println(adj_set);
					writer.write(graph.getNodeName(edge.getSourceNode()).replace(" ", "_") + " ");
					writer.write(graph.getNodeName(edge.getTargetNode()).replace(" ", "_"));
					if (weighted) {
						writer.write(" " + String.format("%.2f", graph.getEdgeWeight(edge)));
					}
					if (edges.hasNext()) {
						writer.write("\n");
					}
				}
			}
		}
		catch (Exception e) {
			throw new AdapterException(e);
		}
		finally {
			try {
				writer.close();
			}
			catch (Exception e) {
			}
		}
	}
	
}
