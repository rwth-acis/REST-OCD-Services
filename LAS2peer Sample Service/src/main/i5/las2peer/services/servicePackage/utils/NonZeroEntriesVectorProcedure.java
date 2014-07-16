package i5.las2peer.services.servicePackage.utils;

import org.la4j.vector.functor.VectorProcedure;

public class NonZeroEntriesVectorProcedure implements VectorProcedure {

	private int nonZeroEntryCount = 0;
	
	@Override
	public void apply(int index, double value) {
		if(value != 0d) {
			nonZeroEntryCount++;
		}
	}

	public int getNonZeroEntryCount() {
		return nonZeroEntryCount;
	}

}
