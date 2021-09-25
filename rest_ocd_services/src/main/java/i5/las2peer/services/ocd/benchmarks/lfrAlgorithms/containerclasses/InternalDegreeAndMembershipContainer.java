package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses;

import java.util.ArrayList;

/**
 * Container class for 'internal_degree_and_membership' method. 
 * Purpose of this class is to hold variables that are modified in the C++
 * version of the algorithm, where inputs are pointers to the variables. Since
 * Java has no pointer mechanics like C++, instance of this class will be returned.
 */
public class InternalDegreeAndMembershipContainer {

	private ArrayList<ArrayList<Integer>> member_matrix;
	private ArrayList<Integer> degree_seq_in;
	private ArrayList<Integer> degree_seq_out;
	private ArrayList<Integer> num_seq;
	private ArrayList<Integer> internal_degree_seq_in;
	private ArrayList<Integer> internal_degree_seq_out;


	public InternalDegreeAndMembershipContainer(ArrayList<ArrayList<Integer>> member_matrix, ArrayList<Integer> degree_seq_in, ArrayList<Integer> degree_seq_out,
			ArrayList<Integer> num_seq, ArrayList<Integer> internal_degree_seq_in, ArrayList<Integer> internal_degree_seq_out) {
		super();
		this.member_matrix = member_matrix;
		this.degree_seq_in = degree_seq_in;
		this.degree_seq_out = degree_seq_out;
		this.num_seq = num_seq;
		this.internal_degree_seq_in = internal_degree_seq_in;
		this.internal_degree_seq_out = internal_degree_seq_out;
	}


	public ArrayList<ArrayList<Integer>> getMember_matrix() {
		return member_matrix;
	}


	public void setMember_matrix(ArrayList<ArrayList<Integer>> member_matrix) {
		this.member_matrix = member_matrix;
	}


	public ArrayList<Integer> getDegree_seq_in() {
		return degree_seq_in;
	}


	public void setDegree_seq_in(ArrayList<Integer> degree_seq_in) {
		this.degree_seq_in = degree_seq_in;
	}


	public ArrayList<Integer> getDegree_seq_out() {
		return degree_seq_out;
	}


	public void setDegree_seq_out(ArrayList<Integer> degree_seq_out) {
		this.degree_seq_out = degree_seq_out;
	}


	public ArrayList<Integer> getNum_seq() {
		return num_seq;
	}


	public void setNum_seq(ArrayList<Integer> num_seq) {
		this.num_seq = num_seq;
	}


	public ArrayList<Integer> getInternal_degree_seq_in() {
		return internal_degree_seq_in;
	}


	public void setInternal_degree_seq_in(ArrayList<Integer> internal_degree_seq_in) {
		this.internal_degree_seq_in = internal_degree_seq_in;
	}


	public ArrayList<Integer> getInternal_degree_seq_out() {
		return internal_degree_seq_out;
	}


	public void setInternal_degree_seq_out(ArrayList<Integer> internal_degree_seq_out) {
		this.internal_degree_seq_out = internal_degree_seq_out;
	}




}
