package i5.las2peer.services.servicePackage.adapters.coverOutput;

import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.FileWriter;
import java.io.Writer;

import y.base.Node;
import y.base.NodeCursor;

public class LabeledMembershipMatrixOutputAdapter extends AbstractCoverOutputAdapter {

	@Override
	public boolean writeCover(Cover cover) {
		boolean writingSucceeded = true;
		Writer writer = null;
		try {
			writer = new FileWriter(filename);
			CustomGraph graph = cover.getGraph();
			NodeCursor nodes = graph.nodes();
			while(nodes.ok()) {
				Node node = nodes.node();
				writer.write(graph.getNodeName(node) + " ");
				for(int i=0; i<cover.communityCount(); i++) {
					writer.write(cover.getBelongingFactor(node, i) + " ");
				}
				writer.write("\n");
				nodes.next();
			}
		}
		catch (Exception e) {
			writingSucceeded = false;
		}
		finally {
			try {
				writer.close();
			}
			catch (Exception e) {
			}
		}
		return writingSucceeded;
	}

	
	
}
