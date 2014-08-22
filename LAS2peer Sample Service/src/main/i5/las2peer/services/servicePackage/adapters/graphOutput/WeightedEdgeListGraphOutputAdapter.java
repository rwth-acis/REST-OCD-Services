package i5.las2peer.services.servicePackage.adapters.graphOutput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.Writer;

import y.base.Edge;
import y.base.EdgeCursor;

/**
 * A graph output adapter for the weighted edge list format.
 * 
 * @author Sebastian
 * 
 */
public class WeightedEdgeListGraphOutputAdapter extends AbstractGraphOutputAdapter {

	/**
	 * Creates a new instance.
	 * @param writer The writer the graph will be written by.
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
			while(edges.ok()) {
				edge = edges.edge();
				writer.write(graph.getNodeName(edge.source()) + " ");
				writer.write(graph.getNodeName(edge.target()) + " ");
				writer.write(String.format("%.5f\n", graph.getEdgeWeight(edge)));
				edges.next();
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
