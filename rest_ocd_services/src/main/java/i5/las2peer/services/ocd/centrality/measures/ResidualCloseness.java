package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

import org.graphstream.algorithm.Dijkstra;
import org.la4j.matrix.Matrix;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.algo.ShortestPaths;
import org.graphstream.graph.Edge;

import org.graphstream.graph.Node;


/**
 * Implementation of Residual Centrality.
 * See: Dangalchev, Chavdar. 2006. Residual closeness in networks.
 * @author Tobias
 *
 */
public class ResidualCloseness implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.RESIDUAL_ClOSENESS, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		// If there are less than 3 nodes
		if(graph.getNodeCount() < 3) {
			while(nc.hasNext()) {
				res.setNodeValue(nc.next(), 0);
			}
			return res;
		}
		
		// Calculate the network closeness (for normalization)
		double[] edgeWeights = graph.getEdgeWeights();
		double networkCloseness = 0.0;	
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

			for(double d : dist) {
				if(d != 0) {
					networkCloseness += 1.0/Math.pow(2, d);
				}
			}
		}
		
		Matrix A = graph.getNeighbourhoodMatrix();
		int n = graph.getNodeCount();
		Node[] nodes = graph.nodes().toArray(Node[]::new);
		// Remove and re-add each node (by removing its edges)
		for(int k = 0; k < n; k++) {
			Node currentNode = nodes[k];
			// Remove edges
			Iterator<Edge> currentNodeEdges = currentNode.edges().iterator();
			while(currentNodeEdges.hasNext()) {
				graph.removeEdge(currentNodeEdges.next());
			}
			
			nc = graph.iterator();
			double[] newEdgeWeights = graph.getEdgeWeights();
			double distSum = 0.0;		
			// Calculate the sum of distances in the graph without the current node
			while(nc.hasNext()) {
				if(Thread.interrupted()) {
					throw new InterruptedException();
				}
				Node node = nc.next();
				double[] dist = new double[graph.getNodeCount()];

				//TODO: Check if dijkstra computation similar enough to old yFiles one, figure out length attribute
				//ShortestPaths.dijkstra(graph, node, true, newEdgeWeights, dist);
				Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, "result", "length");
				dijkstra.init(graph);
				dijkstra.setSource(node);
				dijkstra.compute();

				for(double d : dist) {
					if(d != 0) {
						distSum += 1.0/Math.pow(2, d);
					}
				}
			}
			res.setNodeValue(currentNode, networkCloseness/distSum);
			
			// Recreate edges
			for(int i = 0; i < n; i++) {
				double weight = A.get(currentNode.getIndex(), i);
				if(weight != 0) {
					Edge newEdge = graph.addEdge(UUID.randomUUID().toString(), currentNode, nodes[i]);
					graph.setEdgeWeight(newEdge, weight);
				}
			}
			for(int i = 0; i < n; i++) {
				double weight = A.get(i, currentNode.getIndex());
				if(weight != 0) {
					Edge newEdge = graph.addEdge(UUID.randomUUID().toString(), nodes[i], currentNode);
					graph.setEdgeWeight(newEdge, weight);
				}
			}
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
		return CentralityMeasureType.RESIDUAL_ClOSENESS;
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
