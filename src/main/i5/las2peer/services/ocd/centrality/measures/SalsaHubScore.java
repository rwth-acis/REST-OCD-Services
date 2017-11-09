package i5.las2peer.services.ocd.centrality.measures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.utils.MatrixOperations;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;

public class SalsaHubScore implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.SALSA_HUB_SCORE, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		int n = graph.nodeCount();
		// If the graph contains no edges
		if(graph.edgeCount() == 0) {
			NodeCursor nc = graph.nodes();
			while(nc.ok()) {
				Node node = nc.node();
				res.setNodeValue(node, 0);
				nc.next();
			}
			return res;
		}

		// Create bipartite graph
		CustomGraph bipartiteGraph = new CustomGraph();
		Node[] nodes = graph.getNodeArray();
		Edge[] edges = graph.getEdgeArray();
		Map<Node, Node> hubNodeMap = new HashMap<Node, Node>();
		Map<Node, Node> authorityNodeMap = new HashMap<Node, Node>();
		Map<Node, Node> reverseHubNodeMap = new HashMap<Node, Node>();
		
		// Create the nodes of the new bipartite graph
		for(Node node : nodes) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			if(node.outDegree() > 0) {
				Node hubNode = bipartiteGraph.createNode();
				hubNodeMap.put(node, hubNode);
				reverseHubNodeMap.put(hubNode, node);
			}
			if(node.inDegree() > 0) {
				Node authorityNode = bipartiteGraph.createNode();
				authorityNodeMap.put(node, authorityNode);
			}
		}
		
		// Add the edges of the new bipartite graph
		for(Edge edge : edges) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node oldSource = edge.source();
			Node oldTarget = edge.target();
			Node newSource = hubNodeMap.get(oldSource);
			Node newTarget = authorityNodeMap.get(oldTarget);
			Edge newEdge = bipartiteGraph.createEdge(newSource, newTarget);
			bipartiteGraph.setEdgeWeight(newEdge, graph.getEdgeWeight(edge));
		}
		
		// Construct matrix
		Matrix hubMatrix = new CCSMatrix(n, n);
		for(Node ih : hubNodeMap.values()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node i = reverseHubNodeMap.get(ih);
			NodeCursor stepOne = ih.successors();
			while(stepOne.ok()) {
				Node ka = stepOne.node();
				NodeCursor stepTwo = ka.predecessors();
				while(stepTwo.ok()) {
					Node jh = stepTwo.node();
					Node j = reverseHubNodeMap.get(jh);			
					double edgeWeightIK = bipartiteGraph.getEdgeWeight(ih.getEdgeTo(ka));
					double edgeWeightJK = bipartiteGraph.getEdgeWeight(jh.getEdgeTo(ka));
					double weightedOutDegreeI = bipartiteGraph.getWeightedOutDegree(ih);
					double weightedInDegreeK = bipartiteGraph.getWeightedInDegree(ka);		
					double oldHij = hubMatrix.get(i.index(), j.index());
					double newHij = oldHij + (double)edgeWeightIK/weightedOutDegreeI * (double)edgeWeightJK/weightedInDegreeK;
					hubMatrix.set(i.index(), j.index(), newHij);
					stepTwo.next();
				}	
				stepOne.next();
			}
		}	
		// Calculate stationary distribution of hub Markov chain
		Vector hubVector = MatrixOperations.calculateStationaryDistribution(hubMatrix);
		
		NodeCursor nc = graph.nodes();	
		while(nc.ok()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.node();
			res.setNodeValue(node, hubVector.get(node.index()));
			nc.next();
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
		return CentralityMeasureType.SALSA_HUB_SCORE;
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
