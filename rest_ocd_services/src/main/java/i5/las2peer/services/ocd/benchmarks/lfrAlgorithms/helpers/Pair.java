package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.helpers;

/**
 * The purpose of this class is to imitate Pair type from C++, with adjustments
 * that allows 'second' to be of any type, while 'first' is an integer
 */
public class Pair<T> {
	private Integer first;
	private T second;

	/**
	 * Constructor for the Pair class
	 * 
	 * @param first  Integer value representing 'first' (key) of the Pair
	 * @param second T value representing 'second' (value) of the Pair
	 */
	public Pair(Integer first, T second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Method to find hashcode for the Pair type
	 */
	@Override
	public int hashCode() {
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;

		return (hashSecond + hashFirst) * hashSecond + hashFirst;
	}

	/**
	 * Getter for 'first' field of the Pair
	 * 
	 * @return Integer representing 'first'
	 */
	public Integer getFirst() {
		return first;
	}

	/**
	 * Getter for 'second' field of the Pair
	 * 
	 * @return T representing 'second'
	 */
	public T getSecond() {
		return second;
	}

	/**
	 * Setter for 'first' of the Pair
	 * 
	 * @param first Integer to be set as 'first' of the Pair
	 */
	public void setFirst(Integer first) {
		this.first = first;
	}

	/**
	 * Setter for 'second' of the Pair
	 * 
	 * @param second T to be set as 'second' of the Pair
	 */
	public void setSecond(T second) {
		this.second = second;
	}

	/**
	 * Equality function for the Pair type, based on comparison of 'first' and
	 * 'second' fields of the Pair type
	 */
	@Override
	public boolean equals(Object other) {
		// Pairs are equal when both 'first' and 'second' fields are equal
		if ((((Pair<?>) other).getFirst() == this.getFirst()) && (((Pair<?>) other).getSecond() == this.getSecond())
				|| (((Pair<?>) other).getFirst().equals(this.getFirst())
						&& (((Pair<?>) other).getSecond().equals(this.getSecond())))) {
			return true;
		}
		return false;
	}

	/**
	 * Function to convert Pair type to String
	 */
	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}

}
