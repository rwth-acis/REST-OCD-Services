package i5.las2peer.services.ocd.algorithms.utils;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import i5.las2peer.services.ocd.graphs.CustomGraph;

import y.base.Node;
import y.base.NodeCursor;



/**
 * The class is used to find all maximal cliques in a graph. It was introduced by 
 * Etsuji Tomita, Akira Tanaka and Haruhisa Takahashi in their paper "The worst-case 
 * time complexity for generating all maximal cliques and computational experiments" in 2006. 
 * 
 * @author Marlene Damm
 *
 */
public class MaximalCliqueGraphRepresentation{
	
	private HashSet<HashSet<Node>> maxCliques;
	
	private Node maxNode; 
	
	public MaximalCliqueGraphRepresentation() {
		this.maxCliques = new HashSet<HashSet<Node>>();
	}
	
	/**
	 * Method to find all maximal cliques of a graph.  
	 * @param graph: the graph in which to find the all maximal cliques
	 */
	public HashSet<HashSet<Node>> cliques(CustomGraph graph) {
		Node[] nodes = graph.getNodeArray();
		HashSet<Node> subgr = new HashSet<Node>(Arrays.asList(nodes));
		HashSet<Node> cand = subgr;
		HashSet<Node> q = new HashSet<Node>();
		expand(q,subgr,cand);
		System.out.println(maxCliques);
		return maxCliques;
	}
	
	/**
	 * Recursive function to find all the maximal cliques in depth-first search approach with pruning 
	 * to make it useful even on big graphs
	 * @param subgr: set of vertices in which is needed to find a complete subgraph. It is defined as
	 * the set of all vertices with are not neighbors of the current largest complete subgraph. 
	 * @param cand: All the vertices which not have been processed by the algorithm
	 */
	protected void expand(HashSet<Node> maxClq, HashSet<Node> subgr, HashSet<Node> cand){
		if(subgr.isEmpty() == true) {
			System.out.println(" clique,");
			maxCliques.add(maxClq);
			System.out.println(maxCliques);
		}else{
			int maxcount = 0; 
			HashSet<Node> maxOverlap = new HashSet<Node>();
			for(Node v: subgr) {
				NodeCursor neighbors = v.neighbors();
				int count = 0;
				HashSet<Node> overlap = new HashSet<Node>(); 
				
				for(int i = 1 ; i <neighbors.size(); i++) {
					Node u = neighbors.node();
					
					if(cand.contains(u)) {
						count++;
						overlap.add(u);
					}
				    if(neighbors.ok()== true){
				    	neighbors.cyclicNext();
				    }
				    else {
				    	break;
				    }
				}
				if(count > maxcount){
					maxNode = v;
				    maxcount = count; 
				    maxOverlap = overlap; 
				}
			}
			HashSet<Node> Ext_u = new HashSet<Node>(cand);
			Ext_u.removeAll(maxOverlap);
			HashSet<Node> q_neighbors = new HashSet<Node>();
			for(Node q: Ext_u) {
					System.out.println(q);
					maxClq.add(q);
					NodeCursor n = q.neighbors();
					
					for(int i = 0 ; i <n.size(); i++) {
						q_neighbors.add(n.node());
						
						if(n.ok()== true){
							n.cyclicNext();
						}
						else {
							break;
						}
					}
					
					HashSet<Node> subgr2 = new HashSet<Node>(subgr);
					HashSet<Node> cand2 = new HashSet<Node>(cand);
					//System.out.println("neighbors");
					//System.out.println(q_neighbors);
					
					subgr2.retainAll(q_neighbors);
				
					cand2.retainAll(q_neighbors);
					expand(maxClq,subgr2,cand2);
					
					cand.remove(q);
					maxClq.remove(q);
			}
			
		}
	}
}		
		