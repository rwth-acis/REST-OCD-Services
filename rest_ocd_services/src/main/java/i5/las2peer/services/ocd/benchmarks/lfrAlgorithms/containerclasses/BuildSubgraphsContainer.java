package i5.las2peer.services.ocd.benchmarks.lfrAlgorithms.containerclasses;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Container class for the outputs of 'build_subgraphs' method. Purpose of this
 * class is to hold variables that are modified in the C++ version of the
 * algorithm, where inputs are pointers to the variables. Since Java has no
 * pointer mechanics like C++, instance of this class will be returned.
 */

public class BuildSubgraphsContainer {

	private ArrayList<TreeSet<Integer>> Ein;
	private ArrayList<TreeSet<Integer>> Eout;
	private ArrayList<ArrayList<Integer>> member_list;
	private ArrayList<ArrayList<Integer>> link_list_in;
	private ArrayList<ArrayList<Integer>> link_list_out;

	public BuildSubgraphsContainer(ArrayList<TreeSet<Integer>> ein, ArrayList<TreeSet<Integer>> eout,
			ArrayList<ArrayList<Integer>> member_list, ArrayList<ArrayList<Integer>> link_list_in,
			ArrayList<ArrayList<Integer>> link_list_out) {
		super();
		Ein = ein;
		Eout = eout;
		this.member_list = member_list;
		this.link_list_in = link_list_in;
		this.link_list_out = link_list_out;
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

	public ArrayList<ArrayList<Integer>> getMember_list() {
		return member_list;
	}

	public void setMember_list(ArrayList<ArrayList<Integer>> member_list) {
		this.member_list = member_list;
	}

	public ArrayList<ArrayList<Integer>> getLink_list_in() {
		return link_list_in;
	}

	public void setLink_list_in(ArrayList<ArrayList<Integer>> link_list_in) {
		this.link_list_in = link_list_in;
	}

	public ArrayList<ArrayList<Integer>> getLink_list_out() {
		return link_list_out;
	}

	public void setLink_list_out(ArrayList<ArrayList<Integer>> link_list_out) {
		this.link_list_out = link_list_out;
	}

}
