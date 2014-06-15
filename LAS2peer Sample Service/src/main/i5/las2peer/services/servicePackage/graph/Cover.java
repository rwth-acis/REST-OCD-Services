package i5.las2peer.services.servicePackage.graph;

import java.util.ArrayList;
import java.util.List;

import org.la4j.matrix.Matrix;

import y.base.Node;

public class Cover {

	private CustomGraph graph;
	private Matrix memberships;
	
	public Cover(CustomGraph graph, Matrix memberships) {
		this.graph = graph;
		this.memberships = memberships;
	}

	public CustomGraph getGraph() {
		return graph;
	}

	public void setGraph(CustomGraph graph) {
		this.graph = graph;
	}

	public Matrix getMemberships() {
		return memberships;
	}

	public void setMemberships(Matrix memberships) {
		this.memberships = memberships;
	}
	
	public int communityCount() {
		return memberships.columns();
	}
	
	public List<Integer> getCommunityIndices(int nodeIndex) {
		List<Integer> communities = new ArrayList<Integer>();
		for(int j=0; j < memberships.columns(); j++) {
			if(memberships.get(nodeIndex, j) > 0) {
				communities.add(j);
			}
		}
		return communities;
	}
	
	public List<Integer> getCommunityIndices(Node node) {
		List<Integer> communities = new ArrayList<Integer>();
		for(int j=0; j < memberships.columns(); j++) {
			if(memberships.get(node.index(), j) > 0) {
				communities.add(j);
			}
		}
		return communities;
	}
	
	public double getBelongingFactor(Node node, int communityIndex) {
		return memberships.get(node.index(), communityIndex);
	}

}
