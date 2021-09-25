package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Container class for 'weights' method. Purpose of this class is to hold
 * variables that are modified in the C++ version of the algorithm, where inputs
 * are pointers to the variables. Since Java has no pointer mechanics like C++,
 * instance of this class will be returned.
 */
public class WeightsContainerClass {

	private ArrayList<TreeMap<Integer, Double>> neigh_weigh_in;
	private ArrayList<TreeMap<Integer, Double>> neigh_weigh_out;

	public WeightsContainerClass(ArrayList<TreeMap<Integer, Double>> neigh_weigh_in,
			ArrayList<TreeMap<Integer, Double>> neigh_weigh_out) {
		super();
		this.neigh_weigh_in = neigh_weigh_in;
		this.neigh_weigh_out = neigh_weigh_out;
	}

	public ArrayList<TreeMap<Integer, Double>> getNeigh_weigh_in() {
		return neigh_weigh_in;
	}

	public void setNeigh_weigh_in(ArrayList<TreeMap<Integer, Double>> neigh_weigh_in) {
		this.neigh_weigh_in = neigh_weigh_in;
	}

	public ArrayList<TreeMap<Integer, Double>> getNeigh_weigh_out() {
		return neigh_weigh_out;
	}

	public void setNeigh_weigh_out(ArrayList<TreeMap<Integer, Double>> neigh_weigh_out) {
		this.neigh_weigh_out = neigh_weigh_out;
	}

}
