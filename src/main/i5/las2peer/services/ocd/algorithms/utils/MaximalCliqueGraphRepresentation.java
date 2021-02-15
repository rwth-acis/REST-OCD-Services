package i5.las2peer.services.ocd.algorithms.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.Parameterizable;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2D;


/**
 * The class is used to find all maximal cliques in a graph. It was introduced by 
 * Etsuji Tomita, Akira Tanaka and Haruhisa Takahashi in their paper "The worst-case 
 * time complexity for generating all maximal cliques and computational experiments" in 2006. 
 * 
 * @author Marlene Damm
 *
 */
public class MaximalCliqueGraphRepresentation{
	
	public MaximalCliqueGraphRepresentation() {}
	
	/**
	 * Method to find all maximal cliques of a graph.  
	 * @param graph: the graph in which to find the all maximal cliques
	 */
	public void cliques(CustomGraph graph) {
		HashSet<Node> subgr = new HashSet<Node>((Collection<? extends Node>) graph.nodes());
		HashSet<Node> cand = subgr;
		expand(subgr,cand);
	}
	
	/**
	 * Recursive function to find all the maximal cliques in depth-first search approach with pruning 
	 * to make it useful even on big graphs
	 * @param subgr: set of vertices in which is needed to find a complete subgraph. It is defined as
	 * the set of all vertices with are not neighbors of the current largest complete subgraph. 
	 * @param cand: All the vertices which not have been processed by the algorithm
	 */
	protected void expand(HashSet<Node> subgr, HashSet<Node> cand){
		if(subgr.isEmpty() != true) {
			int maxcount = 0; 
			HashSet<Node> maxOverlap = new HashSet<Node>();
			for(Node v: subgr) {
				NodeCursor neighbors = v.neighbors();
				int count = 0;
				HashSet<Node> overlap = new HashSet<Node>(); 
				for(int i = 0 ; i <neighbors.size(); i++) {
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
				    maxcount = count; 
				    maxOverlap = overlap; 
				}
			}
			HashSet<Node> Ext_u = new HashSet<Node>(cand);
			Ext_u.removeAll(maxOverlap);
			for(Node q: Ext_u) {
				Collection<?> q_neighbors = (Collection<?>) q.neighbors();
				HashSet<Node> subgr2 = new HashSet<Node>(subgr);
				subgr2.retainAll(q_neighbors);
				HashSet<Node> cand2 = new HashSet<Node>(cand);
				cand2.retainAll(q_neighbors);
				expand(subgr2,cand2);
				cand.remove(q);
			}
				
		}
	}
}