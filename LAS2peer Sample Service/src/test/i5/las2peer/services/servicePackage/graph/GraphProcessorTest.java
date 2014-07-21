package i5.las2peer.services.servicePackage.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.algorithms.OcdAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.SskAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;

public class GraphProcessorTest {

	/*
	 * Tests making a graph undirected, i.e. the reverse edge creation
	 * and edge weight setting, on sawmill.
	 */
	@Test
	public void testMakeUndirected() throws AdapterException {
		/*
		 * Note that getSawmillGraph makes use of makeUndirected.
		 */
		CustomGraph undirectedGraph = OcdTestGraphFactory.getSawmillGraph();
		CustomGraph directedGraph = OcdTestGraphFactory.getDirectedSawmillGraph();
		assertEquals(undirectedGraph.nodeCount(), directedGraph.nodeCount());
		/*
		 * Assures that the undirected graph has precisely twice as many edges
		 */
		assertEquals(undirectedGraph.edgeCount(), 2 * directedGraph.edgeCount());
		System.out.println("Edge Count Directed Graph: " + directedGraph.edgeCount());
		System.out.println("Edge Count Undirected Graph: " + undirectedGraph.edgeCount());
		/*
		 * Assures that the undirected graph includes all the original and the reverse edges
		 * and possesses correct edge weights.
		 */
		NodeCursor directedNodes = directedGraph.nodes();
		Node[] undirectedNodes = undirectedGraph.getNodeArray();
		while(directedNodes.ok()) {
			Node directedNode = directedNodes.node();
			Node undirectedNode = undirectedNodes[directedNode.index()];
			NodeCursor directedSuccessors = directedNode.successors();
			while(directedSuccessors.ok()) {
				Node directedSuccessor = directedSuccessors.node();
				Node undirectedSuccessor = undirectedNodes[directedSuccessor.index()];
				Edge edge = directedNode.getEdge(directedSuccessor);
				double weight = directedGraph.getEdgeWeight(edge);
				Edge toEdge = undirectedNode.getEdgeTo(undirectedSuccessor);
				Edge fromEdge = undirectedNode.getEdgeFrom(undirectedSuccessor);
				assertNotNull(toEdge);
				assertNotNull(fromEdge);
				assertEquals(weight, undirectedGraph.getEdgeWeight(toEdge), 0);
				assertEquals(weight, undirectedGraph.getEdgeWeight(fromEdge), 0);
				directedSuccessors.next();
			}
			directedNodes.next();
		}
	}
	
	/*
	 * Tests the component division and cover remerge on simple two components.
	 */
	@Test
	public void testDivideAndMergeConnectedComponents() throws OcdAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleTwoComponentsGraph();
		GraphProcessor processor = new GraphProcessor();
		Map<CustomGraph, Map<Node, Node>> componentMap = processor.divideIntoConnectedComponents(graph);
		Map<Cover, Map<Node, Node>> coverMap = new HashMap<Cover, Map<Node, Node>>();
		Cover currentCover;
		OcdAlgorithm algo = new SskAlgorithm();
		for(Map.Entry<CustomGraph, Map<Node, Node>> entry : componentMap.entrySet()) {
			currentCover = algo.detectOverlappingCommunities(entry.getKey());
			coverMap.put(currentCover, entry.getValue());
		}
		Cover cover = processor.mergeComponentCovers(graph, coverMap);
		System.out.println("Divide and merge of simple two components");
		System.out.println(cover.toString());
	}

}
