package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.la4j.vector.Vectors;
import org.la4j.matrix.Matrix;

import org.graphstream.graph.Node;

/**
 * Implements the modularity metric.
 */
public class ModularityMetric implements StatisticalMeasure {
		
	public ModularityMetric() {
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) {
	}

	@Override
	public Map<String, String> getParameters() {
		return new HashMap<String, String>();
	}
	
	@Override
	public double measure(Cover cover) throws OcdMetricException, InterruptedException, OcdAlgorithmException {
		CustomGraph graph = cover.getGraph(); 
		int edgeCount = graph.getEdgeCount()/2; 
		double modularity = 0;
		Matrix adjacency = graph.getNeighbourhoodMatrix();
		Node[] nodes = graph.nodes().toArray(Node[]::new);
		
		for(int i = 0; i < graph.getNodeCount(); i++) {
			Node n1 = nodes[i];
			double deg1 = graph.getNeighbours(n1).size(); 
			List<Integer> com1 = cover.getCommunityIndices(n1); 
			for(int j = i+1; j < graph.getNodeCount(); j++) {
				Node n2 = nodes[j];
				List<Integer> com2 = cover.getCommunityIndices(n2); 
			    com2.retainAll(com1);
				if(com2.size() != 0) {
					double deg2 = graph.getNeighbours(n2).size();
					modularity -= deg1*deg2/(2*edgeCount);
					if(adjacency.get(i, j) > 0){
						modularity += 1;
					} 
				}
			}
		}
						
		return modularity/edgeCount; 
	}
	
	/**
	 * Returns the graph types which are compatible for a metric.
	 * @return The compatible graph types.
	 */
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.ZERO_WEIGHTS);
		compatibleTypes.add(GraphType.WEIGHTED);
		return compatibleTypes;
	}

}
