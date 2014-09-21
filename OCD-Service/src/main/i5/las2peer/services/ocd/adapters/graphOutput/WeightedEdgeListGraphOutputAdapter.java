package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.Writer;

import y.base.Edge;
import y.base.EdgeCursor;

/**
 * A graph output adapter for weighted edge list format.
 * Each line of output contains first the name of a source node of an edge, then the name of a target node of an edge
 * and finally a double value as the edge weight, using the space character (' ') as a delimiter. There is one line for each edge.
 * @author Sebastian
 * 
 */
public class WeightedEdgeListGraphOutputAdapter extends AbstractGraphOutputAdapter {

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
	
	@Override
	public void writeGraph(CustomGraph graph) throws AdapterException {
		try {
			EdgeCursor edges = graph.edges();
			Edge edge;
			if(edges.ok()) {
				edge = edges.edge();
				edges.next();
				while(edges.ok()) {
					writer.write(graph.getNodeName(edge.source()) + " ");
					writer.write(graph.getNodeName(edge.target()) + " ");
					writer.write(String.format("%.2f", graph.getEdgeWeight(edge)));
					edge = edges.edge();
					edges.next();
					if(edges.ok()) {
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
