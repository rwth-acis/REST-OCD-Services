package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Container class for the outputs of 'propagate' method. Purpose of this class
 * is to hold variables that are modified in the C++ version of the algorithm,
 * where inputs are pointers to the variables. Since Java has no pointer
 * mechanics like C++, instance of this class will be returned.
 */
public class PropagateContainer {

	private ArrayList<TreeMap<Integer, Double>> neigh_weigh_in;
	private ArrayList<TreeMap<Integer, Double>> neigh_weigh_out;
	private ArrayList<ArrayList<Double>> wished;
	private ArrayList<ArrayList<Double>> factual;
	private double tot_var;

	public PropagateContainer(ArrayList<TreeMap<Integer, Double>> neigh_weigh_in,
			ArrayList<TreeMap<Integer, Double>> neigh_weigh_out, ArrayList<ArrayList<Double>> wished,
			ArrayList<ArrayList<Double>> factual, double tot_var) {
		super();
		this.neigh_weigh_in = neigh_weigh_in;
		this.neigh_weigh_out = neigh_weigh_out;
		this.wished = wished;
		this.factual = factual;
		this.tot_var = tot_var;
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

	public ArrayList<ArrayList<Double>> getWished() {
		return wished;
	}

	public void setWished(ArrayList<ArrayList<Double>> wished) {
		this.wished = wished;
	}

	public ArrayList<ArrayList<Double>> getFactual() {
		return factual;
	}

	public void setFactual(ArrayList<ArrayList<Double>> factual) {
		this.factual = factual;
	}

	public double getTot_var() {
		return tot_var;
	}

	public void setTot_var(double tot_var) {
		this.tot_var = tot_var;
	}

}
