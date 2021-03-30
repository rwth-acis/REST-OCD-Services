package i5.las2peer.services.ocd.algorithms.utils;

import java.util.Collection;
import org.la4j.vector.Vector;

/**
* The original version of the overlapping community detection algorithm introduced in 2020
* by Ping Ji, Shanxin Zhang, Zhiping Zhou.
* @author Marlene Damm
*/
//TODO description of the algorithm
public class Ant {
	 
	private Vector lambda; 
	private Collection<Integer> neighbors;	 
	private int group; // number of group in which ant is
	private Vector solution;   
	public int number; 
	private Vector fitness; 
	private boolean new_sol; // was new solution added?
	
	public Ant(){
		
	}
	
	public Ant(int i){
		this.number = i; 
	}

	/*----------------------------------------------------------------------------------------------------------------------------------------------------------
	 * getters for some of the global attributes
	 *----------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	public Vector getWeight() {
		return lambda;
	}
	
	public Collection<Integer> getNeighbors() {
		return neighbors;
	}
	 
	public int getGroup() {
		return group; 
	}
	
	public Vector getSolution() {
		return solution; 
	}
	 
	public Vector getFitness() {
		return fitness; 
	}
	 
	 public boolean getNew_sol() {
		return new_sol; 
	}
		 
	/*----------------------------------------------------------------------------------------------------------------------------------------------------------
	 * setters for some of the global attributes
	 *----------------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	 public void setWeight(Vector weight) {
		this.lambda = weight; 
	 }
	 
	 public void setNeighbors(Collection<Integer> collection) {
		this.neighbors = collection; 
	 }
	 
	 public void setGroup(int group) {
			this.group = group; 
	}
	 
	 public void setSolution(Vector solution) {
			this.solution = solution; 
	}
		
	 public void setFitness(Vector fitness) {
			this.fitness = fitness; 
	}
	 
	public void setTrueNew_sol() {
			this.new_sol = true; 
	 }
	
	public void setFalseNew_sol() {
		this.new_sol = false; 
	}
	
}