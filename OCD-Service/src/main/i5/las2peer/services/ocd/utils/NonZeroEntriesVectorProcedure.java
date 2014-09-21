package i5.las2peer.services.ocd.utils;

import java.util.ArrayList;
import java.util.List;

import org.la4j.vector.functor.VectorProcedure;

/**
 * Vector procedure for determining all non zero entries of a vector.
 * @author Sebastian
 *
 */
public class NonZeroEntriesVectorProcedure implements VectorProcedure {

	/**
	 * The indices of all non zero entries.
	 */
	private List<Integer> nonZeroEntries = new ArrayList<Integer>();
	
	@Override
	public void apply(int index, double value) {
		if(value != 0d) {
			nonZeroEntries.add(index);
		}
	}

	/**
	 * Returns the indices of all non zero entries.
	 * Note that the vector procedure must first be executed on a vector.
	 * @return The indices.
	 */
	public List<Integer> getNonZeroEntries() {
		return nonZeroEntries;
	}
	
	/**
	 * Returns the count / amount of non zero entries.
	 * Note that the vector procedure must first be executed on a vector.
	 * @return The count.
	 */
	public int getNonZeroEntryCount() {
		return nonZeroEntries.size();
	}

}
