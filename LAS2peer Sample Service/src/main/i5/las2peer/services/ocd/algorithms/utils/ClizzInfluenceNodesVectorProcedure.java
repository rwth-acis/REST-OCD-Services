package i5.las2peer.services.ocd.algorithms.utils;

import java.util.HashSet;
import java.util.Set;

import org.la4j.vector.functor.VectorProcedure;

public class ClizzInfluenceNodesVectorProcedure implements VectorProcedure {

	private Set<Integer> influencingNodeIndices = new HashSet<Integer>();
	
	/**
	 * Returns for a node the indices of the nodes influencing it.
	 * The procedure must before have run on the node's distance vector (only).
	 * @return The indices of the influencing nodes.
	 */
	public Set<Integer> getInfluencingNodeIndices() {
		return influencingNodeIndices;
	}

	@Override
	public void apply(int i, double value) {
		if(value > 0) {
			influencingNodeIndices.add(i);
		}
	}

}
