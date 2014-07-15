package i5.las2peer.services.servicePackage.algorithms.utils;

import org.la4j.vector.functor.VectorProcedure;

/**
 * A vector procedure calculating leadership values according to the Clizz Algorithm.
 * @author Sebastian
 *
 */
public class ClizzLeadershipVectorProcedure implements VectorProcedure {

	private double leadershipIndex = 0;
	private double influenceFactor = Double.POSITIVE_INFINITY;
	
	/**
	 * Creates an instance of the vector procedure.
	 * @param influenceFactor The influence range used by the algorithm.
	 */
	public ClizzLeadershipVectorProcedure(double influenceFactor) {
		this.influenceFactor = influenceFactor;
	}
	
	/**
	 * Returns the leadership value of a node.
	 * @return The leadership value.
	 * @precondition The vector procedure was run on the node's
	 * distance vector (only).
	 */
	public double getLeadershipIndex() {
		return leadershipIndex;
	}

	@Override
	public void apply(int i, double value) {
		if(value > 0) {
			leadershipIndex += Math.pow(Math.E, - value / influenceFactor);
		}
	}

}
