package i5.las2peer.services.ocd.adapters.coverOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.Writer;

import y.base.Node;
import y.base.NodeCursor;

/**
 * A cover output adapter for the labeled membership matrix format.
 * Each line of output contains first a node name and then n double values (where n is some natural number), using the space character (' ') as a delimiter.
 * There is one line for each node and the i-th double value of a row will define the node's membership degree for the i-th community.
 * @author Sebastian
 *
 */
public class LabeledMembershipMatrixCoverOutputAdapter extends AbstractCoverOutputAdapter {

	/**
	 * Creates a new instance setting the writer attribute.
	 * @param writer The writer used for output.
	 */
	public LabeledMembershipMatrixCoverOutputAdapter(Writer writer) {
		this.setWriter(writer);
	}
	
	public LabeledMembershipMatrixCoverOutputAdapter() {
	}
	
	@Override
	public void writeCover(Cover cover) throws AdapterException {
		try {
			CustomGraph graph = cover.getGraph();
			NodeCursor nodes = graph.nodes();
			while(nodes.ok()) {
				Node node = nodes.node();
				String nodeName = graph.getNodeName(node);
				if(nodeName.isEmpty()) {
					nodeName = Integer.toString(node.index());
				}
				writer.write(nodeName + " ");
				for(int i=0; i<cover.communityCount(); i++) {
					writer.write(String.format("%.4f ", cover.getBelongingFactor(node, i)));
				}
				nodes.next();
				if(nodes.ok()) {
					writer.write("\n");
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
