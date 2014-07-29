package i5.las2peer.services.servicePackage.adapters.coverInput;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.Adapters;
import i5.las2peer.services.servicePackage.algorithms.AlgorithmLog;
import i5.las2peer.services.servicePackage.algorithms.AlgorithmType;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import y.base.Node;
import y.base.NodeCursor;

public class NodeCommunityListInputAdapter extends AbstractCoverInputAdapter {

	public NodeCommunityListInputAdapter(Reader reader) {
		this.setReader(reader);
	}
	
	@Override
	public Cover readCover(CustomGraph graph, AlgorithmLog algorithm)
			throws AdapterException {
		String nodeName;
		Map<String, List<Integer>> nodeCommunities = new HashMap<String,  List<Integer>>();;
		List<Integer> communityIndices;
		Map<String, Integer> communityNames = new HashMap<String, Integer>();
		int communityCount = 0;
		try {
			List<String> line = Adapters.readLine(reader);
			String communityName;
			/*
			 * Reads edges
			 */
			while (line.size() >= 2) {
				nodeName = line.get(0);
				for(int i=1; i<line.size(); i++) {
					communityName = line.get(i);
					if(!communityNames.containsKey(communityName)) {
						communityNames.put(communityName, communityCount);
						communityCount++;
					}
					if (!nodeCommunities.containsKey(nodeName)) {
						communityIndices = new ArrayList<Integer>();
						communityIndices.add(communityNames.get(communityName));
						nodeCommunities.put(nodeName, communityIndices);
					} else {
						nodeCommunities.get(nodeName).add(communityNames.get(communityName));
					}
				}
				line = Adapters.readLine(reader);
			}
		} catch (Exception e) {
			throw new AdapterException(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Matrix memberships = new CCSMatrix(graph.nodeCount(), communityCount);
		NodeCursor nodes = graph.nodes();
		Node node;
		while(nodes.ok()) {
			node = nodes.node();
			nodeName = graph.getNodeName(node);
			communityIndices = nodeCommunities.get(nodeName);
			for(int communityIndex : communityIndices) {
				memberships.set(node.index(), communityIndex, 1d/communityIndices.size());
			}
			nodes.next();
		}
		return new Cover(graph, memberships, new AlgorithmLog(AlgorithmType.UNDEFINED, new HashMap<String, String>()));
	}
	
}
