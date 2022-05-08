package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;


/**
 * Implementation of Stress Centrality.
 * See: Brandes, Ulrik. 2005. Network analysis: methodological foundations.
 * @author Tobias
 *
 */
public class StressCentrality implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		if(graph.getTypes().contains(GraphType.WEIGHTED)) {
			return getValuesWeighted(graph);
		}
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.STRESS_CENTRALITY, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		while(nc.hasNext()) {
			res.setNodeValue(nc.next(), 0);
		}
		nc = graph.iterator();
		
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node s = nc.next();	
			
			// Variable declaration
			Queue<Node> Q = new LinkedList<Node>();
			Stack<Node> S = new Stack<Node>();
			Map<Node, LinkedList<Node>> Pred = new HashMap<Node, LinkedList<Node>>();
			Map<Node, Double> dist = new HashMap<Node, Double>();
			Map<Node, Integer> sigma = new HashMap<Node, Integer>();
			Map<Node, Double> delta = new HashMap<Node, Double>();
			
			// Initialization
			Iterator<Node> iterator = graph.iterator();
			while(iterator.hasNext()) {
				Node w = iterator.next();

				Pred.put(w, new LinkedList<Node>());
				dist.put(w, Double.POSITIVE_INFINITY);
				sigma.put(w, 0);
				
			}
			dist.put(s, 0.0);
			sigma.put(s, 1);
			Q.add(s);
			
			// Calculate single-source shortest-paths
			while(!Q.isEmpty()) {
				Node v = Q.poll();
				S.push(v);
				
				Iterator<Node> outNeighbors = graph.getSuccessorNeighbours(v).iterator();
				while(outNeighbors.hasNext()) {
					Node w = outNeighbors.next();
					
					// Path discovery
					if(dist.get(w) == Double.POSITIVE_INFINITY) {
						dist.put(w, dist.get(v) + 1);
						Q.add(w);
					}
					
					// Path counting
					if(dist.get(w) == dist.get(v) + 1) {
						sigma.put(w, sigma.get(w) + sigma.get(v));
						Pred.get(w).add(v);
					}
					
				}
			}
			
			// Accumulation
			iterator = graph.iterator();
			while(iterator.hasNext()) {
				Node v = iterator.next();	
				delta.put(v, 0.0);
			}
			
			while(!S.isEmpty()) {
				Node w = S.pop();
				for(Node v : Pred.get(w)) {
					delta.put(v, delta.get(v) + (double) sigma.get(v) * (1 + delta.get(w)/sigma.get(w)));
				}
				if(w != s) {
					res.setNodeValue(w, res.getNodeValue(w) + delta.get(w));
				}
			}
		}
		
		// If graph is undirected, divide centrality values by 2
		if(!graph.getTypes().contains(GraphType.DIRECTED)) {
			nc = graph.iterator();
			while(nc.hasNext()) {
				Node node = nc.next();
				res.setNodeValue(node, res.getNodeValue(node)/2);
			}
		}	
		return res;
	}
	
	private CentralityMap getValuesWeighted(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.STRESS_CENTRALITY, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		while(nc.hasNext()) {
			res.setNodeValue(nc.next(), 0);
		}
		nc = graph.iterator();
		
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node s = nc.next();	
			
			// Variable declaration
			Queue<Node> Q = new LinkedList<Node>();
			Stack<Node> S = new Stack<Node>();
			Map<Node, LinkedList<Node>> Pred = new HashMap<Node, LinkedList<Node>>();
			Map<Node, Double> dist = new HashMap<Node, Double>();
			Map<Node, Integer> sigma = new HashMap<Node, Integer>();
			Map<Node, Double> delta = new HashMap<Node, Double>();
			
			// Initialization
			Iterator<Node> iterator = graph.iterator();
			while(iterator.hasNext()) {
				Node w = iterator.next();
				Pred.put(w, new LinkedList<Node>());
				dist.put(w, Double.POSITIVE_INFINITY);
				sigma.put(w, 0);		
			}
			dist.put(s, 0.0);
			sigma.put(s, 1);
			Q.add(s);
			
			// Calculate single-source shortest-paths
			while(!Q.isEmpty()) {
				// Extract node with minimum distance
				Node v = Q.peek();
				for(Node cur : Q) {
					if(dist.get(cur) < dist.get(v))
						v = cur;
				}
				S.push(v);
				Q.remove(v);
				
				// Iterator<Node> outNeighbors = v.successors();
				Iterator<Edge> outEdges = v.leavingEdges().iterator();
				while(outEdges.hasNext()) {
					Edge outEdge = outEdges.next();
					Node w = outEdge.getTargetNode();
					
					// Path discovery
					if(dist.get(w) > dist.get(v) + graph.getEdgeWeight(outEdge)) {
						dist.put(w, dist.get(v) + graph.getEdgeWeight(outEdge));
						if(!Q.contains(w))
							Q.add(w);
						sigma.put(w, 0);
						Pred.put(w, new LinkedList<Node>());
					}
					
					// Path counting
					if(dist.get(w) == dist.get(v) + graph.getEdgeWeight(outEdge)) {
						sigma.put(w, sigma.get(w) + sigma.get(v));
						Pred.get(w).add(v);
					}
					
				}
			}
			
			// Accumulation
			iterator = graph.iterator();
			while(iterator.hasNext()) {
				Node v = iterator.next();	
				delta.put(v, 0.0);
			}
			while(!S.isEmpty()) {
				Node w = S.pop();
				for(Node v : Pred.get(w)) {
					delta.put(v, delta.get(v) + (double) sigma.get(v) * (1 + delta.get(w)/sigma.get(w)));
				}
				if(w != s) {
					res.setNodeValue(w, res.getNodeValue(w) + delta.get(w));
				}
			}
		}
		
		// If graph is undirected, divide centrality values by 2
		if(!graph.getTypes().contains(GraphType.DIRECTED)) {
			nc = graph.iterator();
			while(nc.hasNext()) {
				Node node = nc.next();
				res.setNodeValue(node, res.getNodeValue(node)/2);
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
		return CentralityMeasureType.STRESS_CENTRALITY;
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
