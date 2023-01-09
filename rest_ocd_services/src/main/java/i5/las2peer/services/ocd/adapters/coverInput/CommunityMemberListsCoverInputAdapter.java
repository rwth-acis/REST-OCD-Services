package i5.las2peer.services.ocd.adapters.coverInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.Adapters;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.Reader;
import java.util.*;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;

import org.graphstream.graph.Node;

/**
 * A cover input adapter for the community member list format.
 * Each line of input contains first the name of a community (optional) and then the names of the member nodes, using the space character (' ') as a delimiter.
 * Nodes will be considered to have an equal membership degree for each community they are associated with.
 * @author Sebastian
 *
 */
public class CommunityMemberListsCoverInputAdapter extends AbstractCoverInputAdapter {

	
	 /**
	 * States whether only the member node names or also the community names are given.
	 * @param areCommunityNamesDefined If false, each line of input is considered to only contain the names of the member nodes without a community name in the beginning.
	 * If true, each line must first contain the community name.
	 */
	private boolean communityNamesDefined = false;
	
	/**
	 * Creates a new standard instance.
	 */
	CommunityMemberListsCoverInputAdapter(){
	}
	
	/**
	 * Creates a new instance setting the reader attribute.
	 * @param reader A reader that will be used to receive input from.
	 */
	CommunityMemberListsCoverInputAdapter(Reader reader) {
		this.reader = reader;
	}
	
	public boolean areCommunityNamesDefined() {
		return communityNamesDefined;
	}

	public void setCommunityNamesDefined(boolean communityNamesDefined) {
		this.communityNamesDefined = communityNamesDefined;
	}

	@Override
	public Cover readCover(CustomGraph graph) throws AdapterException {
		String nodeName;
		String communityName;
		Map<String, List<Integer>> nodeCommunities = new HashMap<String,  List<Integer>>();
		List<Integer> communityIndices;
		Map<Integer, String> communityNames = new HashMap<Integer, String>();
		int communityCount = 0;
		try {
			List<String> line = Adapters.readLine(reader);
			/*
			 * Reads edges
			 */
			while (line.size() >= 1) {
				int i=0;
				if(communityNamesDefined) {
					communityName = line.get(0);
					communityNames.put(communityCount, communityName);
					i++;
				}
				for(; i<line.size(); i++) {
					nodeName = line.get(i);
					if (!nodeCommunities.containsKey(nodeName)) {
						communityIndices = new ArrayList<Integer>();
						communityIndices.add(communityCount);
						nodeCommunities.put(nodeName, communityIndices);
					} else {
						nodeCommunities.get(nodeName).add(communityCount);
					}
				}
				communityCount++;
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
		Matrix memberships = new CCSMatrix(graph.getNodeCount(), communityCount);
		Iterator<Node> nodes = graph.iterator();
		Node node;
		while(nodes.hasNext()) {
			node = nodes.next();
			nodeName = graph.getNodeName(node);
			communityIndices = nodeCommunities.get(nodeName);
			if(communityIndices != null) {
				for(int communityIndex : communityIndices) {
					memberships.set(node.getIndex(), communityIndex, 1d/communityIndices.size());
				}
			}
		}
		Cover cover = new Cover(graph, memberships);
		if(communityNamesDefined) {
			for(int i : communityNames.keySet()) {
				cover.setCommunityName(i, communityNames.get(i));
			}
		}
		return cover;
	}

}
