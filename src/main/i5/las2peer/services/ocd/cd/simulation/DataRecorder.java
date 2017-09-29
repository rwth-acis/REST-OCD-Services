
package i5.las2peer.services.ocd.cd.simulation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;

import sim.engine.SimState;
import sim.engine.Steppable;

public class DataRecorder implements Steppable {
	
	private static final long serialVersionUID = 1;

	private List<Double> cooperationValues;
	private List<Double> payoffValues;
	
	
	public DataRecorder() {
		this.cooperationValues = new ArrayList<Double>();
		this.payoffValues = new ArrayList<Double>();
	}
	
	/**
	 * Creates a DataRecorder with preset list sizes.
	 */
	public DataRecorder(int maxIterations) {

		this.cooperationValues = new ArrayList<Double>(maxIterations);
		this.payoffValues = new ArrayList<Double>(maxIterations);
	}	
	
	/**
	 * Creates a DataRecorder with injected data. This Constructor is used for testing purpose.
	 */
	public DataRecorder(List<Double> cooperationValues, List<Double> payoffValues) {

		this.cooperationValues = cooperationValues;
		this.payoffValues = payoffValues;
	}

	/////////////////// Steps ///////////////////////////

	/**
	 * Stores the average cooperation and average payoff value every time it is
	 * stepped.
	 */
	@Override
	public void step(SimState state) {

		Simulation simulation = (Simulation) state;
		cooperationValues.add(simulation.getCooperationValue());
		payoffValues.add(simulation.getAveragePayoff());
	}

	///// Methods /////
	/**
	 * 
	 * @return
	 */
	public boolean isSteady(int generations, double threshold) {
		
		int size = getCooperationValues().size();
		if(size < generations) 
			return false;		
			
		double std = getDeviation(getCooperationValues(), generations);
		if(std < threshold)
			return true;
		return false;
	}
	
	/**
	 * Clear the cooperation and payoff value lists.
	 */
	protected void clear() {
		cooperationValues = new ArrayList<>(cooperationValues.size());
		payoffValues = new ArrayList<>(payoffValues.size());
	}

	public List<Double> getCooperationValues() {
		return this.cooperationValues;
	}
	
	public List<Double> getPayoffValues() {
		return this.payoffValues;
	}

	public double getCooperationValue(int round) {

		return cooperationValues.get(round - 1);
	}

	public double getPayoffValue(int round) {

		return payoffValues.get(round - 1);
	}
	
	public double getAverageCooperativity(int rounds) {		
		return getAverage(getCooperationValues(), rounds);
	}
	
	public double getAverageWealth(int rounds) {
		return getAverage(getPayoffValues(), rounds);
	}
	
	protected double getAverage(List<Double> list, int rounds) {
		
		double[] values = getArray(list, rounds);
		return StatUtils.mean(values);	
	}
	
	protected double getDeviation(List<Double> list, int rounds) {
		
		double[] values = getArray(list, rounds);
		return  Math.sqrt(StatUtils.variance(values));	
	}
	
	protected double[] getArray(List<Double> list, int length) {
		
		int size = list.size();
		if(size < length) 
			length = size;	

		double[] vals = new double[length];
		for (int i = 0; i < length; i++) {
		    vals[i] = list.get(size-i-1);
		}		
		return vals;
	}



}
