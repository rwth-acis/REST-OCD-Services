package i5.las2peer.services.ocd.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.Similarities;
import i5.las2peer.services.ocd.algorithms.utils.Termmatrix;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;

public class ContentBasedWeightingAlgorithm implements OcdAlgorithm{
	
	///////////////////
	////Constructor////
	///////////////////
	
	public ContentBasedWeightingAlgorithm(){
		
	}
	
	@Override
	public CoverCreationType getAlgorithmType() {
		return CoverCreationType.CONTENT_BASED_WEIGHTING_ALGORITHM;
	}
	
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.CONTENT_LINKED);
		return compatibilities;
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public Map<String, String> getParameters() {
		return new HashMap<String, String>();
	}
	
	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) throws InterruptedException, OcdAlgorithmException {
		Termmatrix tm = new Termmatrix(graph);
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
			}
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
			s = s * graph.getEdgeWeight(edge);
			graph.setEdgeWeight(edge, s);
		}
		
		return null;
		
	}

}
