package i5.las2peer.services.servicePackage.graph;

import static org.junit.Assert.*;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import org.junit.Test;

import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;

public class GraphProcessorTest {

	/*
	 * Tests whether the makeUndirected method does indeed create 
	 * all the reverse edges for a graph and also set the
	 * corresponding edge weights correctly.
	 */
	@Test
	public void testMakeUndirected() {
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

}
