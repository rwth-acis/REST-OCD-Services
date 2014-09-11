package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

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
			if(edges.ok()) {
				edge = edges.edge();
				edges.next();
				while(edges.ok()) {
					writer.write(graph.getNodeName(edge.source()) + " ");
					writer.write(graph.getNodeName(edge.target()) + " ");
					writer.write(String.format("%.2f\n", graph.getEdgeWeight(edge)));
					edge = edges.edge();
					edges.next();
				}
				/*
				 * Write last edge without line break
				 */
				writer.write(graph.getNodeName(edge.source()) + " ");
				writer.write(graph.getNodeName(edge.target()) + " ");
				writer.write(String.format("%.2f", graph.getEdgeWeight(edge)));
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
