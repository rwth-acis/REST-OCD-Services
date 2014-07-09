package i5.las2peer.services.servicePackage.utils;

import java.util.List;

import org.la4j.vector.Vector;

public class CustomVectors{

	/**
	 * Sets all entries of the vector which are below the threshold to zero.
	 * @param vector The vector.
	 * @param threshold The threshold.
	 */
	public static void setEntriesBelowThresholdToZero(Vector vector, double threshold) {
		BelowThresholdEntriesVectorProcedure procedure = new BelowThresholdEntriesVectorProcedure(threshold);
		vector.eachNonZero(procedure);
		List<Integer> belowThresholdEntries = procedure.getBelowThresholdEntries();
		for(int i : belowThresholdEntries) {
			vector.set(i, 0);
		}
	}

}