package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * This class is meant to imitate C++ Multimap, where elements are stored as
 * pairs that are sorted based on 'first' field of the Pair (which can be seen
 * as keys)
 */
public class CustomMultiMap<T extends Comparable<T>> implements Iterable<Pair<T>> {

	private ArrayList<Pair<T>> map = new ArrayList<Pair<T>>();

	/**
	 * add specified Pair to the multimap
	 * 
	 * @param pair pair to be added
	 */
	public void put(Pair<T> pair) {
		map.add(pair);
		Collections.sort(map, new PairComparatorKeysOnly<T>());
	}

	/**
	 * @return arraylist storing the pairs
	 */
	public ArrayList<Pair<T>> getMultiMap() {
		Collections.sort(map, new PairComparatorKeysOnly<T>());
		return map;
	}

	/**
	 * @return number of elements in the multimap
	 */
	public int size() {
		return map.size();
	}

	/**
	 * Gets a pair located at a specified index
	 * 
	 * @param index index of a pair to get
	 * @return pair at specified index
	 */
	public Pair<T> get(int index) {
		return map.get(index);
	}

	/**
	 * Removes the pair from the multimap to which an input iterator is pointing to
	 * 
	 * @param it iterator pointing to a pair to remove
	 * @return true if removal was successful
	 */
	public boolean erase(Iterator<Pair<T>> it) {
		Pair<T> to_remove = it.next();
		return map.remove(to_remove);
	}

	/**
	 * Removes the pair specified in input from the multimap
	 * 
	 * @param pair pair to be removed
	 * @return true if removal was successful
	 */
	public boolean erase(Pair<T> pair) {
		Pair<T> to_remove = pair;
		return map.remove(pair);
	}

	/**
	 * Create an iterator starting from a specified index (iterator goes backwards)
	 * 
	 * @param index index at which the iterator should start
	 * @return Iterator to the multimap at a specified index
	 */
	public CustomMultiMapIterator<T> iterator(int index) {
		Collections.sort(map, new PairComparatorKeysOnly<T>());
		return new CustomMultiMapIterator<>(this, index);
	}

	/**
	 * Create an iterator starting at the end of the Multimap (iterator goes
	 * backwards)
	 * 
	 * @return Iterator to the multimap
	 */
	@Override
	public CustomMultiMapIterator<T> iterator() {
		Collections.sort(map, new PairComparatorKeysOnly<T>());
		return new CustomMultiMapIterator<T>(this);
	}
}
