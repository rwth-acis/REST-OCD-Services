package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.algorithm.Dijkstra;
import y.algo.ShortestPaths;
import org.graphstream.graph.Node;


/**
 * Implementation of Radiality.
 * See: Valente, Thomas W. and Foreman, Robert K. 1998. Integration and radiality: measuring the extent of an individual's connectedness and reachability in a network.
 * @author Tobias
 *
 */
public class Radiality implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.RADIALITY, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		// If there is only a single node
		if(graph.getNodeCount() == 1) {
			res.setNodeValue(nc.next(), 0);
			return res;
		}
		
		// Calculate the sum of distances and the number of reachable nodes for all nodes and find the diameter of the graph
		double[] edgeWeights = graph.getEdgeWeights();
		Map<Node, Integer> reachableNodes = new HashMap<Node, Integer>();
		double maxDistance = 0;
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();
			double[] dist = new double[graph.getNodeCount()];

			//TODO: Check if dijkstra computation similar enough to old yFiles one, figure out length attribute
			//ShortestPaths.dijkstra(graph, node, true, edgeWeights, dist);
			Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, "result", "length");
			dijkstra.init(graph);
			dijkstra.setSource(node);
			dijkstra.compute();

			double distSum = 0.0;
			int reachableNodesCounter = 0;
			for(double d : dist) {
				if(d != Double.POSITIVE_INFINITY && d != 0) {
					distSum += d;
					reachableNodesCounter++;
					if(d > maxDistance)
						maxDistance = d;
				}
			}
			reachableNodes.put(node, reachableNodesCounter);
			res.setNodeValue(node, distSum);
		}
		
		// Reverse distances
		nc = graph.iterator();
		while(nc.hasNext()) {
			Node node = nc.next();
			double distSum = res.getNodeValue(node);
			/*
			 * Each distance in the sum is reversed which is equivalent to multiplying the number of reachable nodes with the 
			 * diameter of the graph and subtracting the sum of distances (+ 1 added to differentiate disconnected nodes).
			 */
			res.setNodeValue(node, (reachableNodes.get(node) * (1 + maxDistance) - distSum)/(graph.getNodeCount()-1));
		}
		return res;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.DIRECTED);
		compatibleTypes.add(GraphType.WEIGHTED);
		return compatibleTypes;
	}

	@Override
	public CentralityMeasureType getCentralityMeasureType() {
		return CentralityMeasureType.RADIALITY;
	}
	
	@Override
	public HashMap<String, String> getParameters() {
		return new HashMap<String, String>();
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
}
