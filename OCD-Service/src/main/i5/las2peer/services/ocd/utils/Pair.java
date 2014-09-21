package i5.las2peer.services.ocd.utils;

/**
 * Generic pair data structure.
 * @author Sebastian
 *
 * @param <F> The type of the first pair element.
 * @param <S> The type of the second pair element.
 */
public class Pair<F, S> {
	
	/**
	 * The first element of the pair.
	 */
    private F first;
    
    /**
     * The second element of the pair.
     */
    private S second;

    /**
     * Creates a new instance.
     */
    public Pair() {
    }
    
    /**
     * Creates a new instance.
     * @param first Sets first.
     * @param second Sets second.
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Setter for first.
     * @param first Sets first.
     */
    public void setFirst(F first) {
        this.first = first;
    }

    /**
     * Setter for second.
     * @param second Sets second.
     */
    public void setSecond(S second) {
        this.second = second;
    }

    /**
     * Getter for first.
     * @return The first object.
     */
    public F getFirst() {
        return first;
    }

    /**
     * Getter for second.
     * @return The second object.
     */
    public S getSecond() {
        return second;
    }

    @Override
    public String toString() {
    	String firstString = "NULL";
    	String secondString = "NULL";
    	if(first != null) {
    		firstString = first.toString();
    	}
    	if(second != null) {
    		secondString = second.toString();
    	}
    	return "[ " + firstString + ", " + secondString + " ]";
    }
    
    @Override
    public boolean equals(Object o) {
    	if(o instanceof Pair<?, ?>) {
    		Pair<?, ?> p = (Pair<?, ?>) o;
    		return (this.first.equals(p.first) && this.second.equals(p.second));
    	}
    	else return false;
    }
    
    @Override
    public int hashCode() {
    	return first.hashCode() + second.hashCode();
    }
}
