package i5.las2peer.services.ocd.utils;

public class Pair<F, S> {
	
    private F first;
    private S second;

    public Pair() {
    }
    
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

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
