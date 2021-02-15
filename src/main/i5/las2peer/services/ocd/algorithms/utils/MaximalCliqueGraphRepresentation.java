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
 * The 
 * @author Marlene
 *
 */
public class MaximalCliqueGraphRepresentation{
	/**
	 * collection of node numbers in a subgraph. Over the iteration of the Expand method this
	 * set is expanding until we reach a maximal complete subgraph. 
	 */
	
	public MaximalCliqueGraphRepresentation()
	{
	}
	
	/**
	 * Method to find all maximal cliques of a graph.  
	 * @param graph: the graph in which to find the all maximal cliques
	 */
	public void cliques(CustomGraph graph) {
		HashSet<Node> subgr = new HashSet<Node>((Collection<? extends Node>) graph.nodes());
		HashSet<Node> cand = subgr;
		expand(subgr,cand);
	}
	
	protected void expand(HashSet<Node> subgr, HashSet<Node> cand){
		if(subgr.length != 0) {
			Node maxNode = subgr[0];
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
				    maxNode = v; 
				    maxOverlap = overlap; 
				}
			}
			HashSet<Node> Ext_u = cand.clone();
			Ext_u.removeAll(maxOverlap);
			HashSet<Node> subgr2 = new HashSet<Node>();
			for(Node q: Ext_u) {
				compSubgr.add(q);
				
			}
				
		}
	}
}