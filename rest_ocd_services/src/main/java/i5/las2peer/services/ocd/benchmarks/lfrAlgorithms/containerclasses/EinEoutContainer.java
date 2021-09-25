package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Container class for return values of 'build_subgraph' method. Purpose of this
 * class is to hold variables that are modified in the C++ version of the
 * algorithm, where inputs are pointers to the variables. Since Java has no
 * pointer mechanics like C++, instance of this class will be returned.
 */
public class EinEoutContainer {

	private ArrayList<TreeSet<Integer>> Ein;
	private ArrayList<TreeSet<Integer>> Eout;

	public EinEoutContainer(ArrayList<TreeSet<Integer>> ein, ArrayList<TreeSet<Integer>> eout) {
		super();
		Ein = ein;
		Eout = eout;

	}

	public ArrayList<TreeSet<Integer>> getEin() {
		return Ein;
	}

	public void setEin(ArrayList<TreeSet<Integer>> ein) {
		Ein = ein;
	}

	public ArrayList<TreeSet<Integer>> getEout() {
		return Eout;
	}

	public void setEout(ArrayList<TreeSet<Integer>> eout) {
		Eout = eout;
	}

}
