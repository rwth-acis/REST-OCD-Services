package i5.las2peer.services.ocd.algorithms.utils;

import java.util.HashSet;

/**
 * This is a helper class that represents Weak cliques as described in the paper
 * 'A Fast Overlapping Community Detection Algorithm Based on Weak Cliques for
 * Largte-Scale Networks' written by Xingui Zhan, Congtao Wang, Yansen Su,
 * Linqiang Pan and Hai-Feng Zhang. In this implementation, nodes are
 * represented by indices corresponding to their row in a network matrix
 *
 */
public class WeakClique {

	/*
	 * Set of node indices belonging to the weak clique instance
	 */
	private HashSet<Integer> nodes; 
	
	/*
	 * Whether the weak clique was visited by the algorithm or not (used when
	 * merging weak cliques)
	 */
	private boolean visited;
	
	
	/**
	 * Constructor without parameters, that creates empty weak clique
	 */
	public WeakClique() {
		
		this.nodes = new HashSet<Integer>();
		this.visited = false;
		
	}
	
	/**
	 * Constructor which creates a weak clique using a set of nodes (node indices)
	 * @param nodes      Set of nodes with which the weak clique should be instantiated
	 */
	public WeakClique(HashSet<Integer> nodes) {
		
		this.nodes = nodes;
		this.visited = false;
		
	}
	
	/**
	 * Adds input node (node index) to the weak clique
	 * @param i     A node to be added to the weak clique
	 */
	public void add(Integer i) {
		
		this.nodes.add(i);
		
	}
	
	/**
	 * Removes input node (node index) from the weak clique
	 * @param i     A node to be removed from the weak clique
	 * @return      true if removal was successful
	 */
	public boolean remove(Integer i) {
		
		return this.nodes.remove(i);
		
	}
	
	/**
	 * Adds all elements of the input set (node indices) to the weak clique
	 * @param set     A set of nodes to be added to the weak clique
	 * @return        true if addition was successful
	 */
	public boolean addAll(HashSet<Integer> set) {
		
		return this.nodes.addAll(set);
		
	}

	/**
	 * Finds the size of a weak clique
	 * @return    Weak clique size
	 */
	public int size() {
		
		return this.nodes.size();
		
	}
	
	/**
	 * Checks if the weak clique contains specified node (node index)
	 * @param node_index     A node index to be checked
	 * @return               true if node is contained
	 */
	public boolean contains(int node_index) {
		
		return this.nodes.contains(node_index);
		
	}


	public HashSet<Integer> getNodes() {
		
		return nodes;
		
	}


	public void setNodes(HashSet<Integer> nodes) {
		
		this.nodes = nodes;
		
	}


	public boolean isVisited() {
		
		return visited;
		
	}


	public void setVisited(boolean visited) {
		
		this.visited = visited;
		
	}


	@Override
	public String toString() {
		
		return  nodes.toString();
		
	}


	@Override
	public int hashCode() {
		
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
		
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WeakClique other = (WeakClique) obj;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}
	
	
	
	
	
	
	
	
}
