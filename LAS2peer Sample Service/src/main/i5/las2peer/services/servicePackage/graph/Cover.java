package i5.las2peer.services.servicePackage.graph;

import java.util.ArrayList;
import java.util.List;

import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;

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
	
	/**
	 * Normalizes each row of the membership matrix using the one norm.
	 * A row stays equal if the sum of the absolute values of all entries equals 0.
	 */
	public void doNormalize() {
		for(int i=0; i<memberships.rows(); i++) {
			Vector row = memberships.getRow(i);
			double norm = row.fold(Vectors.mkManhattanNormAccumulator());
			if(norm != 0) {
				row = row.divide(norm);
				memberships.setRow(i, row);
			}
		}
	}

}
