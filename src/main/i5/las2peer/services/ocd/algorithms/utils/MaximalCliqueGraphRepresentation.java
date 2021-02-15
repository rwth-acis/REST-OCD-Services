package i5.las2peer.services.ocd.algorithms.utils;

import java.util.Arrays;
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

public class MaximalCliqueGraphRepresentation{
	/**
	 * collection of node numbers in a subgraph. Over the iteration of the Expand method this
	 * set is expanding until we reach a maximal complete subgraph. 
	 */
	private HashSet<Integer> compSubgr;
	
	private Node[] subgr;
	
	private List<Node> cand; 
	
	
	public MaximalCliqueGraphRepresentation(CustomGraph graph)
	{
	        this.subgr = graph.getNodeArray();
	        this.cand = Arrays.asList(subgr.clone());
	}
	
	protected void expand() {
		if(subgr.length != 0) {
			Node maxNode = subgr[0];
			int maxcount = 0; 
			for(Node v: subgr) {
				NodeCursor neighbors = v.neighbors();
				int count = 0;
				
				for(int i = 0 ; i <neighbors.size(); i++) {
					Node u = neighbors.node();
					if(cand.contains(u)) {
						count++;
					}
				    if(neighbors.ok()== true){
				    	neighbors.cyclicNext();
				    }
				if(count>)
				    
				}
				while 
			}
				
		}
	}

}