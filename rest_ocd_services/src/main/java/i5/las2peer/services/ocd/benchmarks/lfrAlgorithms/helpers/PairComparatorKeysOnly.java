package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers;

import java.util.Comparator;

/**
 * Comparator for the custom Pair class that is only based on 'first' field. 
 */
public class PairComparatorKeysOnly<T extends Comparable<T>> implements Comparator<Pair<T>>{

	/**
	 * Compare Pairs based on 'first'
	 */
	@Override
	public int compare(Pair<T> o1, Pair<T> o2) {
		
		return o1.getFirst().compareTo(o2.getFirst());
	}

}