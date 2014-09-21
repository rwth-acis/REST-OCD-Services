package i5.las2peer.services.ocd.graphs;

import java.util.ArrayList;
import java.util.List;

import org.la4j.vector.functor.VectorProcedure;

/**
 * Vector procedure for determining the indices of vector entries below a threshold value.
 * @author Sebastian
 *
 */
public class BelowThresholdEntriesVectorProcedure implements VectorProcedure {

	private double threshold;
	private List<Integer> belowThresholdEntries = new ArrayList<Integer>();
	
	/**
	 * Creates a new instance for a specific threshold.
	 * @param threshold The threshold.
	 */
	protected BelowThresholdEntriesVectorProcedure(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * Returns the entries. Note that the procedure must be run on a vector first.
	 * @return The indices of the vector entries below the threshold.
	 */
	protected List<Integer> getBelowThresholdEntries() {
		return belowThresholdEntries;
	}

	@Override
	public void apply(int i, double value) {
		if (value < threshold) {
			belowThresholdEntries.add(i);
		}
	}

}
