package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses;

import java.util.ArrayList;

/**
 * Container class for 'Combinatorics.add_factors' method. Purpose of this class is
 * to hold variables that are modified in the C++ version of the algorithm,
 * where inputs are pointers to the variables. Since Java has no pointer
 * mechanics like C++, instance of this class will be returned.
 */
public class FactorsContainer {

	private ArrayList<Double> num;
	private ArrayList<Double> den;

	public FactorsContainer(ArrayList<Double> num, ArrayList<Double> den) {
		super();
		this.num = num;
		this.den = den;
	}

	public ArrayList<Double> getNum() {
		return num;
	}

	public void setNum(ArrayList<Double> num) {
		this.num = num;
	}

	public ArrayList<Double> getDen() {
		return den;
	}

	public void setDen(ArrayList<Double> den) {
		this.den = den;
	}

}
