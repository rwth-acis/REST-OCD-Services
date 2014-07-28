package i5.las2peer.services.servicePackage.adapters.coverOutput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.Writer;

import y.base.Node;
import y.base.NodeCursor;

public class LabeledMembershipMatrixOutputAdapter extends AbstractCoverOutputAdapter {

	public LabeledMembershipMatrixOutputAdapter(Writer writer) {
		this.setWriter(writer);
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
					writer.write(String.format("%.3f ", cover.getBelongingFactor(node, i)));
				}
				writer.write("\n");
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
