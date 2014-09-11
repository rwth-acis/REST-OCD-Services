package i5.las2peer.services.ocd.adapters.coverInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.Adapters;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import y.base.Node;
import y.base.NodeCursor;


public class LabeledMembershipMatrixCoverInputAdapter extends AbstractCoverInputAdapter {

	public LabeledMembershipMatrixCoverInputAdapter(Reader reader) {
		this.setReader(reader);
	}
	
	public LabeledMembershipMatrixCoverInputAdapter() {
	}
	
	@Override
	public Cover readCover(CustomGraph graph) throws AdapterException {
		NodeCursor nodes = graph.nodes();
		Node node;
		Map<String, Node> reverseNodeNames = new HashMap<String, Node>();
		while(nodes.ok()) {
			node = nodes.node();
			reverseNodeNames.put(graph.getNodeName(node), node);
			nodes.next();
		}
		try {
			List<String> line = Adapters.readLine(reader);
			Matrix memberships = new CCSMatrix(graph.nodeCount(), line.size() - 1);
			int nodeIndex;
			double belongingFactor;
			while(line.size() > 0) {
				nodeIndex = reverseNodeNames.get(line.get(0)).index();
				for(int i=1; i<line.size(); i++) {
					belongingFactor = Double.parseDouble(line.get(i));
					memberships.set(nodeIndex, i-1, belongingFactor);
				}
				line = Adapters.readLine(reader);
			}
			return new Cover(graph, memberships);
		}
		catch (Exception e) {
			throw new AdapterException(e);
		}
		finally {
			try {
				reader.close();
			}
			catch (Exception e) {
			}
		}
	}

}
