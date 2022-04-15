package i5.las2peer.services.ocd.algorithms;

import java.util.*;

import org.apache.commons.math3.linear.ArrayRealVector;

import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.Similarities;
import i5.las2peer.services.ocd.algorithms.utils.Termmatrix;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.metrics.ExecutionTime;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

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
		Iterator<Edge> edgeIterator = graph.edges().iterator();
		Iterator<Edge> comp = graph.edges().iterator();
		Edge edge;
		Edge compEdge;
		double max = 0;
		while(edgeIterator.hasNext()){
			edge = edgeIterator.next();
			while(comp.hasNext()){
				compEdge = comp.next();
				if(edge.getSourceNode().equals(compEdge.getSourceNode()) || edge.getTargetNode().equals(compEdge.getSourceNode())){
					double temp = graph.getEdgeWeight(compEdge);
					if(max < temp){
						max = temp;
					}
				}
			}
			comp = graph.edges().iterator();
			graph.setEdgeWeight(edge, graph.getEdgeWeight(edge)/max);
		}
		edgeIterator = graph.edges().iterator();
		
		//compute and combine content-based weight
		while(edgeIterator.hasNext()){
			edge = edgeIterator.next();
			Node source = edge.getSourceNode();
			Node target = edge.getTargetNode();
			ArrayRealVector v = (ArrayRealVector) tm.getMatrix().getRowVector(tm.getNodeIdList().indexOf(source));
			ArrayRealVector u = (ArrayRealVector) tm.getMatrix().getRowVector(tm.getNodeIdList().indexOf(target));
			double s = sim.cosineSim(v, u);
			s = (s + graph.getEdgeWeight(edge))/2;
			graph.setEdgeWeight(edge, s);
		}
		
		return graph;
		
	}

}
