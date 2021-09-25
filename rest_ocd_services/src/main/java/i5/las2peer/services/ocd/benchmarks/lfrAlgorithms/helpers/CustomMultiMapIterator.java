package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers;

import java.util.Iterator;

/**
 * This is an iterator class for the CustomMultiMap. The iterator goes in a
 * reversed order, meaning that calling 'next()' results in a step back, instead
 * of conventional iterators that take a step forward. The purpose of this is to
 * fit the requirements of the C++ version of LFR Algorithm
 */
public class CustomMultiMapIterator<T extends Comparable<T>> implements Iterator<Pair<T>> {
	private CustomMultiMap<T> map;
	private Pair<T> current;
	private int current_index;

	/**
	 * Constructor for the iterator that starts at the end of the multimap (iterator
	 * goes backwards)
	 * 
	 * @param map CustomMultiMap<T> to which the iterator should be created
	 */
	public CustomMultiMapIterator(CustomMultiMap<T> map) {
		this.map = map;
		this.current_index = map.size() - 1; // last position, since we're interested in reverse order iterator
		if (current_index == -1) {
			this.current = null;
		} else {
			this.current = map.get(current_index);
		}
	}

	/**
	 * Constructor for the iterator that starts at a specified index (iterator goes
	 * backwards)
	 * 
	 * @param map   CustomMultiMap<T> to which the iterator should bne created
	 * @param index index at which the iterator should start
	 */
	public CustomMultiMapIterator(CustomMultiMap<T> map, int index) {
		if (index >= map.size()) {
			throw new RuntimeException("trying to create iterator at index which is out of bounds");
		}
		this.map = map;
		this.current_index = index; // last position, since we're interested in reverse order iterator
		if (current_index >= 0) {
			this.current = map.get(current_index);
		} else {
			this.current = null;
		}
	}

	/**
	 * @return true if iterator has next element (iterator goes backwards, so 'next'
	 *         is actually 'previous')
	 */
	@Override
	public boolean hasNext() {
		return current_index >= 0;
	}

	/**
	 * Returns the next element and moves the index of the iterator
	 * 
	 * @return Pair<T> that is next for the iterator (iterator goes backwards, so
	 *         'next' is actually 'previous')
	 */
	@Override
	public Pair<T> next() {
		current = map.get(current_index);
		current_index--;
		return current;
	}

	/**
	 * Purpose of this method is to increase index, when taking a step back is
	 * necessary for the iterator
	 */
	public void goBack() {
		current_index++; // since this iterator iterates in a reversed manner, going back refers to ++
							// isntead of --
	}

	/**
	 * @return index at which the iterator is currently pointing
	 */
	public int getCurrentIndex() {
		return this.current_index;
	}

}
