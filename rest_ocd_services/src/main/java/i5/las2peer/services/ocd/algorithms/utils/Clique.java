package i5.las2peer.services.ocd.algorithms.utils;

import java.util.HashSet;


//Holds Clique meta information
public class Clique extends WeakClique{
    

    //The id of the community this clique is part of.
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
