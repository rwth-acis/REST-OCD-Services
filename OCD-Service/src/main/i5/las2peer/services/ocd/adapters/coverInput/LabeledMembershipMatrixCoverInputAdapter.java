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

/**
 * A cover input adapter for the labeled membership matrix format.
 * Each line of input contains first a node name and then n double values (where n is some natural number), using the space character (' ') as a delimiter.
 * The i-th double value of a row will define the node's membership degree for the i-th community.
 * There must be exactly one line for each node of the graph and each line must have the same number ("n") of double values.
 * @author Sebastian
 *
 */
public class LabeledMembershipMatrixCoverInputAdapter extends AbstractCoverInputAdapter {

	/**
	 * Creates a new instance setting the reader attribute.
	 * @param reader A reader that will be used to receive input from.
	 */
	public LabeledMembershipMatrixCoverInputAdapter(Reader reader) {
		this.setReader(reader);
	}
	
	/**
	 * Creates a new instance.
	 */
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
