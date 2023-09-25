package i5.las2peer.services.ocd.algorithms.utils;

import java.util.HashSet;

public class Clique extends WeakClique{

    private int community;

    public Clique(HashSet<Integer> nodes) {
        super(nodes);
    }

    public Clique () {

    }

    public int getCommunity() {
		
		return community;
		
	}


	public void setCommunity(int community) {
		
		this.community = community;
		
	}
}
