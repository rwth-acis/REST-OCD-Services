package i5.las2peer.services.ocd.adapters.graphOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.Writer;

import y.base.Node;
import y.base.NodeCursor;

/**
 * A graph output adapter for unweighted edge list format, specifically for the Doca Algorithm. Not to be used for general input or output.
 * Each line of output contains first the (id+1) of a source node of an edge, then the (id+1) of a target node of an edge, using the space character (' ') as a delimiter. There is one line for each edge.
 * The edges are (increasingly) sorted by source node id (primary) and target node id (secondary).
 * @author Sebastian
 * 
 */
public class DocaGraphOutputAdapter extends AbstractGraphOutputAdapter {

	boolean weighted = true;
	
	/**
	 * Creates a new instance setting the writer attribute.
	 * @param writer The writer used for output.
	 */
	public DocaGraphOutputAdapter(Writer writer) {
		this.setWriter(writer);
	}

	/**
	 * Creates a new instance.
	 */
	public DocaGraphOutputAdapter() {
	}

	@Override
	public void writeGraph(CustomGraph graph) throws AdapterException {
		try {
			NodeCursor nodes = graph.nodes();
			boolean isFirstEdge = true;
			while(nodes.ok()) {
				Node node = nodes.node();
				NodeCursor neighbors = node.neighbors();
				// Lists each neighbor only once.
				for(int i=0; neighbors.ok() && i < neighbors.size()/2; i++) {
					Node neighbor = neighbors.node();
					// Ensures correct sorting.
					if(neighbor.index() > node.index())
					{
						// Omits empty line at the end of output.
						if(isFirstEdge) {
							isFirstEdge = false;
						}
						else {
							writer.write("\n");
						}
						writer.write(Integer.toString(node.index() + 1) + " ");
						writer.write(Integer.toString(neighbor.index() + 1));
					}
					neighbors.next();
				}
				nodes.next();
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
