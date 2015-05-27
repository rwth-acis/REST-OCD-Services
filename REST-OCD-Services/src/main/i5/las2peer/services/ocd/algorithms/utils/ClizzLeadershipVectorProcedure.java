package i5.las2peer.services.ocd.algorithms.utils;

import org.la4j.vector.functor.VectorProcedure;

/**
 * A vector procedure calculating leadership values used by the Clizz Algorithm.
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
	 * Note that the vector procedure must have been run previously on the node's distance vector.
	 * @return The leadership value.
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
