package i5.las2peer.services.ocd.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.SskAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import i5.las2peer.services.ocd.utils.Pair;

import java.io.FileNotFoundException;
import java.util.*;

import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;


public class GraphProcessorTest {

	/*
	 * Tests making a graph undirected, i.e. the reverse edge creation
	 * and edge weight setting, on sawmill.
	 */
	@Test
	public void testMakeUndirected() throws AdapterException, FileNotFoundException, InterruptedException {
		/*
		 * Note that getSawmillGraph makes use of makeUndirected.
		 */
		CustomGraph undirectedGraph = OcdTestGraphFactory.getSawmillGraph();
		CustomGraph directedGraph = OcdTestGraphFactory.getDirectedSawmillGraph();
		assertEquals(undirectedGraph.getNodeCount(), directedGraph.getNodeCount());
		/*
		 * Assures that the undirected graph has precisely twice as many edges
		 */
		assertEquals(undirectedGraph.getEdgeCount(), 2 * directedGraph.getEdgeCount());
		System.out.println("Edge Count Directed Graph: " + directedGraph.getEdgeCount());
		System.out.println("Edge Count Undirected Graph: " + undirectedGraph.getEdgeCount());
		/*
		 * Assures that the undirected graph includes all the original and the reverse edges
		 * and possesses correct edge weights.
		 */
		Node[] directedNodes = directedGraph.nodes().toArray(Node[]::new);
		Node[] undirectedNodes = undirectedGraph.nodes().toArray(Node[]::new);
		for (Node directedNode : directedNodes) {
			Node undirectedNode = undirectedNodes[directedNode.getIndex()];
			Iterator<Node> directedSuccessors = directedGraph.getSuccessorNeighbours(directedNode).iterator();
			while(directedSuccessors.hasNext()) {
				Node directedSuccessor = directedSuccessors.next();
				Node undirectedSuccessor = undirectedNodes[directedSuccessor.getIndex()];
				Edge edge = directedNode.getEdgeToward(directedSuccessor);
				double weight = directedGraph.getEdgeWeight(edge);
				Edge toEdge = undirectedNode.getEdgeToward(undirectedSuccessor);
				Edge fromEdge = undirectedNode.getEdgeFrom(undirectedSuccessor);
				assertNotNull(toEdge);
				assertNotNull(fromEdge);
				assertEquals(weight, undirectedGraph.getEdgeWeight(toEdge), 0);
				assertEquals(weight, undirectedGraph.getEdgeWeight(fromEdge), 0);
			}
		}
	}

	/*
	 * Tests removing multi edges from a graph.
	 */
	@Test
	public void testRemoveMultiEdges() {
		CustomGraph graph = new CustomGraph();
		Node node1 = graph.addNode("1");
		Node node2 = graph.addNode("2");
		Edge edge1 = graph.addEdge(UUID.randomUUID().toString(), node1, node2);
		graph.setEdgeWeight(edge1, 2d);
		graph.addEdge(UUID.randomUUID().toString(),node1, node2);
		System.out.println("Multi Edge Graph");
		System.out.println("Edge Count: " + graph.getEdgeCount() + "\nEdge Weights:");
		for(Edge edge : graph.edges().toArray(Edge[]::new)) {
			System.out.println(graph.getEdgeWeight(edge));
		}
		GraphProcessor processor = new GraphProcessor();
		processor.removeMultiEdges(graph);
		System.out.println("Single Edge Graph");
		System.out.println("Edge Count: " + graph.getEdgeCount() + "\nEdge Weights:");
		for(Edge edge : graph.edges().toArray(Edge[]::new)) {
			System.out.println(graph.getEdgeWeight(edge));
		}
		assertEquals(1, graph.getEdgeCount());
		assertEquals(3d, graph.getEdgeWeight(graph.getEdge(0)), 0.00001);
	}
	
	/*
	 * Tests the component division and cover remerge on simple two components.
	 */
	@Test
	public void testDivideAndMergeConnectedComponents() throws OcdAlgorithmException, InterruptedException, OcdMetricException {
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
		Node node0 = graph.addNode("0");
		Node node1 = graph.addNode("1");
		graph.addEdge(UUID.randomUUID().toString(), node0, node1);
		processor.determineGraphTypes(graph);
		System.out.println("One directed edge.");
		System.out.println(graph.getTypes());
		assertEquals(1, graph.getTypes().size());
		assertTrue(graph.isOfType(GraphType.DIRECTED));
		/*
		 * One undirected edge.
		 */
		graph.addEdge(UUID.randomUUID().toString(), node1, node0);
		processor.determineGraphTypes(graph);
		System.out.println("One undirected edge.");
		System.out.println(graph.getTypes());
		assertEquals(0, graph.getTypes().size());
		/*
		 * Undirected edge and self loop.
		 */
		Edge edge2 = graph.addEdge(UUID.randomUUID().toString(), node0, node0);
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
		Node node2 = graph.addNode("2");
		Edge edge3 = graph.addEdge(UUID.randomUUID().toString(), node0, node2);
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
		/*
		 * Undirected edge, 0 weight self loop and directed negative edge.
		 */
		graph.setPath("testPath");
		processor.determineGraphTypes(graph);
		System.out.println("Undirected edge, 0 weight self loop and directed negative edge.");
		System.out.println(graph.getTypes());
		assertEquals(6, graph.getTypes().size());
		assertTrue(graph.isOfType(GraphType.SELF_LOOPS));
		assertTrue(graph.isOfType(GraphType.WEIGHTED));
		assertTrue(graph.isOfType(GraphType.ZERO_WEIGHTS));
		assertTrue(graph.isOfType(GraphType.DIRECTED));
		assertTrue(graph.isOfType(GraphType.NEGATIVE_WEIGHTS));		
		assertTrue(graph.isOfType(GraphType.CONTENT_LINKED));		
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
		Node node0 = graph.addNode("0");
		Node node1 = graph.addNode("1");
		Node node2 = graph.addNode("2");
		Node node3 = graph.addNode("3");
		graph.addEdge(UUID.randomUUID().toString(), node0, node1);
		graph.addEdge(UUID.randomUUID().toString(), node1, node0);
		graph.addEdge(UUID.randomUUID().toString(), node0, node0);
		Edge edge3 = graph.addEdge(UUID.randomUUID().toString(), node0, node2);
		Edge edge4 = graph.addEdge(UUID.randomUUID().toString(), node1, node2);
		Edge edge5 = graph.addEdge(UUID.randomUUID().toString(), node2, node1);
		Edge edge6 = graph.addEdge(UUID.randomUUID().toString(), node2, node3);
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
