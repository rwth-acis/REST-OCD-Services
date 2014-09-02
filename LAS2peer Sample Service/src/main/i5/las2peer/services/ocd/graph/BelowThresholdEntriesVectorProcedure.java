package i5.las2peer.services.ocd.graph;

import java.util.ArrayList;
import java.util.List;

import org.la4j.vector.functor.VectorProcedure;

/*
 * Used for obtaining the indices of vector entries below a threshold value.
 * @author Sebastian
 *
 */
public class BelowThresholdEntriesVectorProcedure implements VectorProcedure {

	private double threshold;
	private List<Integer> belowThresholdEntries = new ArrayList<Integer>();
	
	protected BelowThresholdEntriesVectorProcedure(double threshold) {
		this.threshold = threshold;
	}

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
