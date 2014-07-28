package i5.las2peer.services.servicePackage.utils;

import java.util.ArrayList;
import java.util.List;

import org.la4j.vector.functor.VectorProcedure;

public class NonZeroEntriesVectorProcedure implements VectorProcedure {

	private List<Integer> nonZeroEntries = new ArrayList<Integer>();
	
	@Override
	public void apply(int index, double value) {
		if(value != 0d) {
			nonZeroEntries.add(index);
		}
	}

	public List<Integer> getNonZeroEntries() {
		return nonZeroEntries;
	}
	
	public int getNonZeroEntryCount() {
		return nonZeroEntries.size();
	}

}
