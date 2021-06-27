package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers;

import java.util.Comparator;

/**
 * Comparator for the custom Pair class that compares using 'first' field of a
 * Pair and then using 'second' field of a Pair
 */
public class PairComparator<T extends Comparable<T>> implements Comparator<Pair<T>>{

	/**
	 * Compare Pairs based on 'first' followed by 'second
	 */
	@Override
	public int compare(Pair<T> o1, Pair<T> o2) {
		
		Integer key1 = o1.getFirst();
		Integer key2 = o2.getFirst();
		
		int comp = key1.compareTo(key2);
		
		if (comp != 0) {
			return comp;
		}
		
		T value1 = o1.getSecond();
		T value2 = o2.getSecond();
		
		return value1.compareTo(value2);
		
		
		
	}

}



