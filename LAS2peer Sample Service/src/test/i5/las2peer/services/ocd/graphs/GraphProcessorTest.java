package i5.las2peer.services.ocd.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.SskAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import i5.las2peer.services.ocd.utils.Pair;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	public void testMakeUndirected() throws AdapterException, FileNotFoundException {
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
	 * Tests removing multi edges from a graph.
	 */
	@Test
	public void testRemoveMultiEdges() {
		CustomGraph graph = new CustomGraph();
		Node node1 = graph.createNode();
		Node node2 = graph.createNode();
		Edge edge1 = graph.createEdge(node1, node2);
		graph.setEdgeWeight(edge1, 2d);
		graph.createEdge(node1, node2);
		System.out.println("Multi Edge Graph");
		System.out.println("Edge Count: " + graph.edgeCount() + "\nEdge Weights:");
		for(Edge edge : graph.getEdgeArray()) {
			System.out.println(graph.getEdgeWeight(edge));
		}
		GraphProcessor processor = new GraphProcessor();
		processor.removeMultiEdges(graph);
		System.out.println("Single Edge Graph");
		System.out.println("Edge Count: " + graph.edgeCount() + "\nEdge Weights:");
		for(Edge edge : graph.getEdgeArray()) {
			System.out.println(graph.getEdgeWeight(edge));
		}
		assertEquals(1, graph.edgeCount());
		assertEquals(3d, graph.getEdgeWeight(graph.getEdgeArray()[0]), 0.00001);
	}
	
	/*
	 * Tests the component division and cover remerge on simple two components.
	 */
	@Test
	public void testDivideAndMergeConnectedComponents() throws OcdAlgorithmException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleTwoComponentsGraph();
		GraphProcessor processor = new GraphProcessor();
		List<Pair<CustomGraph, Map<Node, Node>>> components = processor.divideIntoConnectedComponents(graph);
		List<Pair<Cover, Map<Node, Node>>> componentCovers = new ArrayList<Pair<Cover, Map<Node, Node>>>();
		Cover currentCover;
		OcdAlgorithm algo = new SskAlgorithm();
		for(Pair<CustomGraph, Map<Node, Node>> component : components) {
			currentCover = algo.detectOverlappingCommunities(component.getFirst());
			componentCovers.add(new Pair<Cover, Map<Node, Node>>(currentCover, component.getSecond()));
		}
		Cover cover = processor.mergeComponentCovers(graph, componentCovers);
		System.out.println("Divide and merge of simple two components");
		System.out.println(cover.toString());
	}

	@Test
	public void testDetermineGraphTypes() {
		System.out.println("Test Determine Graph Types");
		/*
		 * Empty graph.
		 */
		CustomGraph graph = new CustomGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.determineGraphTypes(graph);
		assertEquals(0, graph.getTypes().size());
		System.out.println("Empty graph.");
		System.out.println(graph.getTypes());
		/*
		 * One directed edge.
		 */
		Node node0 = graph.createNode();
		Node node1 = graph.createNode();
		graph.createEdge(node0, node1);
		processor.determineGraphTypes(graph);
		System.out.println("One directed edge.");
		System.out.println(graph.getTypes());
		assertEquals(1, graph.getTypes().size());
		assertTrue(graph.isOfType(GraphType.DIRECTED));
		/*
		 * One undirected edge.
		 */
		graph.createEdge(node1, node0);
		processor.determineGraphTypes(graph);
		System.out.println("One undirected edge.");
		System.out.println(graph.getTypes());
		assertEquals(0, graph.getTypes().size());
		/*
		 * Undirected edge and self loop.
		 */
		Edge edge2 = graph.createEdge(node0, node0);
		processor.determineGraphTypes(graph);
		System.out.println("Undirected edge and self loop.");
		System.out.println(graph.getTypes());
		assertEquals(1, graph.getTypes().size());
		assertTrue(graph.isOfType(GraphType.SELF_LOOPS));
		/*
		 * Undirected edge and weighted self loop.
		 */
		graph.setEdgeWeight(edge2, 2.5);
		processor.determineGraphTypes(graph);
		System.out.println("Undirected edge and weighted self loop.");
		System.out.println(graph.getTypes());
		assertEquals(2, graph.getTypes().size());
		assertTrue(graph.isOfType(GraphType.SELF_LOOPS));
		assertTrue(graph.isOfType(GraphType.WEIGHTED));
		/*
		 * Undirected edge and 0 weight self loop.
		 */
		graph.setEdgeWeight(edge2, 0);
		processor.determineGraphTypes(graph);
		System.out.println("Undirected edge and 0 weight self loop.");
		System.out.println(graph.getTypes());
		assertEquals(3, graph.getTypes().size());
		assertTrue(graph.isOfType(GraphType.SELF_LOOPS));
		assertTrue(graph.isOfType(GraphType.WEIGHTED));
		assertTrue(graph.isOfType(GraphType.ZERO_WEIGHTS));
		/*
		 * Undirected edge, 0 weight self loop and directed negative edge.
		 */
		Node node2 = graph.createNode();
		Edge edge3 = graph.createEdge(node0, node2);
		graph.setEdgeWeight(edge3, -1);
		processor.determineGraphTypes(graph);
		System.out.println("Undirected edge, 0 weight self loop and directed negative edge.");
		System.out.println(graph.getTypes());
		assertEquals(5, graph.getTypes().size());
		assertTrue(graph.isOfType(GraphType.SELF_LOOPS));
		assertTrue(graph.isOfType(GraphType.WEIGHTED));
		assertTrue(graph.isOfType(GraphType.ZERO_WEIGHTS));
		assertTrue(graph.isOfType(GraphType.DIRECTED));
		assertTrue(graph.isOfType(GraphType.NEGATIVE_WEIGHTS));		
	}
	
	@Test
	public void testMakeCompatible() {
		/*
		 * Empty Graph
		 */
		System.out.println("Test Get Compatible Graph");
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		CustomGraph graph = new CustomGraph();
		GraphProcessor processor = new GraphProcessor();
		processor.determineGraphTypes(graph);
		System.out.println("Empty Graph. No compatible type.");
		CustomGraph copy = new CustomGraph(graph);
		processor.makeCompatible(copy, compatibleTypes);
		processor.determineGraphTypes(copy);
		System.out.println("Types: " + copy.getTypes());
		assertEquals(compatibleTypes, copy.getTypes());
		/*
		 * Further init.
		 */
		compatibleTypes.add(GraphType.NEGATIVE_WEIGHTS);
		compatibleTypes.add(GraphType.WEIGHTED);
		compatibleTypes.add(GraphType.DIRECTED);
		compatibleTypes.add(GraphType.SELF_LOOPS);
		Node node0 = graph.createNode();
		Node node1 = graph.createNode();
		Node node2 = graph.createNode();
		Node node3 = graph.createNode();
		graph.createEdge(node0, node1);
		graph.createEdge(node1, node0);
		graph.createEdge(node0, node0);
		Edge edge3 = graph.createEdge(node0, node2);
		Edge edge4 = graph.createEdge(node1, node2);
		Edge edge5 = graph.createEdge(node2, node1);
		Edge edge6 = graph.createEdge(node2, node3);
		graph.setEdgeWeight(edge3, 3);
		graph.setEdgeWeight(edge4, -1);
		graph.setEdgeWeight(edge5, 0);
		graph.setEdgeWeight(edge6, 0);
		processor.determineGraphTypes(graph);
		/*
		 * No zero weights.
		 */
		System.out.println("No zero weights.");
		copy = new CustomGraph(graph);
		processor.makeCompatible(copy, compatibleTypes);
		processor.determineGraphTypes(copy);
		System.out.println("Types: " + copy.getTypes());
		assertEquals(compatibleTypes, copy.getTypes());
		/*
		 * No negative weights.
		 */
		compatibleTypes.remove(GraphType.NEGATIVE_WEIGHTS);
		compatibleTypes.add(GraphType.ZERO_WEIGHTS);
		copy = new CustomGraph(graph);
		processor.makeCompatible(copy, compatibleTypes);
		processor.determineGraphTypes(copy);
		System.out.println("No negative weights.");
		System.out.println("Types: " + copy.getTypes());
		assertEquals(compatibleTypes, copy.getTypes());
		/*
		 * Undirected.
		 */
		compatibleTypes.add(GraphType.NEGATIVE_WEIGHTS);
		compatibleTypes.remove(GraphType.DIRECTED);
		copy = new CustomGraph(graph);
		processor.makeCompatible(copy, compatibleTypes);
		processor.determineGraphTypes(copy);
		System.out.println("Undirected.");
		System.out.println("Types: " + copy.getTypes());
		assertEquals(compatibleTypes, copy.getTypes());
		/*
		 * Self loops. 
		 */
		compatibleTypes.add(GraphType.DIRECTED);
		compatibleTypes.remove(GraphType.SELF_LOOPS);
		copy = new CustomGraph(graph);
		processor.makeCompatible(copy, compatibleTypes);
		processor.determineGraphTypes(copy);
		System.out.println("Self loops.");
		System.out.println("Types: " + copy.getTypes());
		assertEquals(compatibleTypes, copy.getTypes());
		/*
		 * Unweighted.
		 */
		compatibleTypes.add(GraphType.SELF_LOOPS);
		compatibleTypes.remove(GraphType.WEIGHTED);
		copy = new CustomGraph(graph);
		processor.makeCompatible(copy, compatibleTypes);
		processor.determineGraphTypes(copy);
		compatibleTypes.remove(GraphType.NEGATIVE_WEIGHTS);
		compatibleTypes.remove(GraphType.ZERO_WEIGHTS);
		System.out.println("Unweighted.");
		System.out.println("Types: " + copy.getTypes());
		assertEquals(compatibleTypes, copy.getTypes());
		/*
		 * Unweighted, no negative weights, no zero weights.
		 */
		copy = new CustomGraph(graph);
		processor.makeCompatible(copy, compatibleTypes);
		processor.determineGraphTypes(copy);
		System.out.println("Unweighted, no negative weights, no zero weights.");
		System.out.println("Types: " + copy.getTypes());
		assertEquals(compatibleTypes, copy.getTypes());
	}
}
