package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.algorithm.flow.FlowAlgorithmBase;
import org.graphstream.algorithm.flow.FordFulkersonAlgorithm;
import y.algo.NetworkFlows;
import y.base.DataProvider;
import org.graphstream.graph.Edge;

import y.base.EdgeMap;
import org.graphstream.graph.Node;

import y.util.Maps;

/**
 * Implementation of Flow Centrality.
 * See: Freeman, Linton C and Borgatti, Stephen P and White, Douglas R. 1991. Centrality in valued graphs: A measure of betweenness based on network flow. 
 * @author Tobias
 *
 */
public class FlowBetweenness implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.FLOW_BETWEENNESS, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));

		Node[] nodeArray = graph.nodes().toArray(Node[]::new);

		FordFulkersonAlgorithm fordFulkerson = new FordFulkersonAlgorithm();
		fordFulkerson.init(graph);



		Iterator<Edge> edgesIt = graph.edges().iterator();
		while(edgesIt.hasNext()) {
			Edge edge = edgesIt.next();
			if(graph.isWeighted()) {
				double modifiedWeight = 0.0f;
				modifiedWeight = graph.getEdgeWeight(edge);

				fordFulkerson.setCapacity(edge.getSourceNode(), edge.getTargetNode(), fordFulkerson.getCapacity(edge.getSourceNode(), edge.getTargetNode()) + modifiedWeight);
			}
			else {
				fordFulkerson.setCapacity(edge.getSourceNode(), edge.getTargetNode(), 1.0);
			}
		}
		
		// Set initial values to 0
		Iterator<Node> nc = graph.iterator();
		while(nc.hasNext()) {
			res.setNodeValue(nc.next(), 0.0);
		}
		
		// For each pair (i,j) of nodes calculate the maximum flow and add flows through the individual nodes to their centrality values
		for(int i = 0; i < nodeArray.length; i++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node source = nodeArray[i];
			for(int j = 0; j < nodeArray.length; j++) {
				if(i != j) {
					Node sink = nodeArray[j];
					
					// Instantiate data structures
					Map<Edge, Integer> flowMap = new HashMap<Edge, Integer>();
					EdgeMap flowEdgeMap = Maps.createEdgeMap(flowMap);
					
					// Calculate maximum flows with given source and sink
					//TODO: Check whether yFiles and graphstream work differently with calculating flow when there are multi-edges or simply forward/backward edges
					//int maximumFlow = NetworkFlows.calcMaxFlow(graph, source, sink, capacities, flowEdgeMap);
					fordFulkerson.setSourceId(source.getId());
					fordFulkerson.setSinkId(sink.getId());

					if(Objects.equals(source.getId(), "1") && Objects.equals(sink.getId(), "0")) { //TODO: Remove
						System.out.println();
					}

					fordFulkerson.compute();
					double maximumFlow = fordFulkerson.getMaximumFlow();

					// Measure flow through all the nodes
					nc = graph.iterator();
					while(nc.hasNext()) {
						Node node = nc.next();
						if(node != source && node != sink && maximumFlow != 0) {
							// Calculate flow through node
							int maximumFlowThroughNode = 0;
							Set<Node> inNeighborsCheck = graph.getPredecessorNeighbours(node);
							Iterator<Node> inNeighbors = graph.getPredecessorNeighbours(node).iterator();
							while(inNeighbors.hasNext()) {
								Node nextneighbor = inNeighbors.next();
								if (!graph.isDirected()) {
									maximumFlowThroughNode += fordFulkerson.getFlow(nextneighbor, node) < 0 ? -fordFulkerson.getFlow(nextneighbor, node) : fordFulkerson.getFlow(nextneighbor, node); //counter-act negative flows from graphstream for back edges in undirected graphs
								}
								else {
									maximumFlowThroughNode += fordFulkerson.getFlow(nextneighbor, node) < 0 ? 0 : fordFulkerson.getFlow(nextneighbor, node); //TODO: Check this with graph that have forth/back edges
								}
							}
							res.setNodeValue(node, res.getNodeValue(node) + maximumFlowThroughNode/maximumFlow);
						}
					}
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
		return CentralityMeasureType.FLOW_BETWEENNESS;
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
