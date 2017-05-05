package i5.las2peer.services.ocd.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.Similarities;
import i5.las2peer.services.ocd.algorithms.utils.Termmatrix;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.metrics.ExecutionTime;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;

public class ContentBasedWeightingAlgorithm{
	
	///////////////////
	////Constructor////
	///////////////////
	
	public ContentBasedWeightingAlgorithm(){
		
	}
	
	
	/*public CoverCreationType getAlgorithmType() {
		return CoverCreationType.CONTENT_BASED_WEIGHTING_ALGORITHM;
	}*/
	

	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.CONTENT_LINKED);
		return compatibilities;
	}
	
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
	

	public Map<String, String> getParameters() {
		return new HashMap<String, String>();
	}
	
	
	public CustomGraph detectOverlappingCommunities(CustomGraph graph, ExecutionTime et) throws InterruptedException, OcdAlgorithmException {
		//et.start();
		et.stop();
		Termmatrix tm = new Termmatrix(graph);
		et.start();
		//et.stop();
		Similarities sim = new Similarities();
		//normalize weights
		EdgeCursor edges = graph.edges();
		EdgeCursor comp = graph.edges();
		Edge edge;
		Edge compEdge;
		double max = 0;
		while(edges.ok()){
			edge = edges.edge();
			while(comp.ok()){
				compEdge = comp.edge();
				if(edge.source().equals(compEdge.source()) || edge.target().equals(compEdge.source())){ 
					double temp = graph.getEdgeWeight(compEdge);
					if(max < temp){
						max = temp;
					}
				}
				comp.next();
			}
			comp.toFirst();
			graph.setEdgeWeight(edge, graph.getEdgeWeight(edge)/max);
			edges.next();
		}
		edges.toFirst();
		
		//compute and combine content-based weight
		while(edges.ok()){
			edge = edges.edge();
			Node source = edge.source();
			Node target = edge.target();
			ArrayRealVector v = (ArrayRealVector) tm.getMatrix().getRowVector(tm.getNodeIdList().indexOf(source));
			ArrayRealVector u = (ArrayRealVector) tm.getMatrix().getRowVector(tm.getNodeIdList().indexOf(target));
			double s = sim.cosineSim(v, u);
			s = (s + graph.getEdgeWeight(edge))/2;
			graph.setEdgeWeight(edge, s);
			edges.next();
		}
		
		return graph;
		
	}

}
