package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Container class for the outputs of following methods: 'propagate_one' and
 * 'propagate_two'. Purpose of this class is to hold variables that are modified
 * in the C++ version of the algorithm, where inputs are pointers to the
 * variables. Since Java has no pointer mechanics like C++, instance of this
 * class will be returned.
 */
public class PropagateOneTwoContainer {

	ArrayList<TreeMap<Integer, Double>> neighbors_weights;
	private ArrayList<ArrayList<Double>> wished;
	private ArrayList<ArrayList<Double>> factual;
	private double tot_var;
	private ArrayList<TreeMap<Integer, Double>> others;

	public PropagateOneTwoContainer(ArrayList<TreeMap<Integer, Double>> neighbors_weights,
			ArrayList<ArrayList<Double>> wished, ArrayList<ArrayList<Double>> factual, double tot_var,
			ArrayList<TreeMap<Integer, Double>> others) {
		super();
		this.neighbors_weights = neighbors_weights;
		this.wished = wished;
		this.factual = factual;
		this.tot_var = tot_var;
		this.others = others;
	}

	public ArrayList<TreeMap<Integer, Double>> getNeighbors_weights() {
		return neighbors_weights;
	}

	public void setNeighbors_weights(ArrayList<TreeMap<Integer, Double>> neighbors_weights) {
		this.neighbors_weights = neighbors_weights;
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

	public ArrayList<TreeMap<Integer, Double>> getOthers() {
		return others;
	}

	public void setOthers(ArrayList<TreeMap<Integer, Double>> others) {
		this.others = others;
	}

}
