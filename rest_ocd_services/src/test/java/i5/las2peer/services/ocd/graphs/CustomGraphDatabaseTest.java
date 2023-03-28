package i5.las2peer.services.ocd.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.utils.DatabaseConfig;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;
import java.util.*;







import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

//@Ignore
public class CustomGraphDatabaseTest {

	private static final String userName1 = "testUser1";
	private static final String graphName1 = "persistenceTestGraph1";
	private static final String invalidGraphName = "invalidGraphName";
	private static Database database;
	
	@BeforeClass
	public static void clearDatabase() {
		database = new Database(true);
	}

	@AfterClass
	public static void deleteDatabase() {
		database.deleteDatabase();
	}
	
	@Test
	public void testPersist() {
		CustomGraph graph = new CustomGraph();
		graph.setUserName(userName1);
		graph.setName(graphName1);
		Node nodeA = graph.addNode("A");
		Node nodeB = graph.addNode("B");
		Node nodeC = graph.addNode("C");
		graph.setNodeName(nodeA, "A");
		graph.setNodeName(nodeB, "B");
		graph.setNodeName(nodeC, "C");
		Edge edgeAB = graph.addEdge(UUID.randomUUID().toString(), nodeA, nodeB);
		graph.setEdgeWeight(edgeAB, 5);
		Edge edgeBC = graph.addEdge(UUID.randomUUID().toString(), nodeB, nodeC);
		graph.setEdgeWeight(edgeBC, 2.5);
		graph.addType(GraphType.DIRECTED);
		
		database.storeGraph(graph);

		List<CustomGraph> queryResults = database.getGraphsbyName(graphName1);
		
		assertEquals(1, queryResults.size());
	    CustomGraph persistedGraph = queryResults.get(0);
	    assertNotNull(persistedGraph);
	    System.out.println("Username: " + persistedGraph.getUserName());
	    System.out.println("Graphname: " + persistedGraph.getName());
	    System.out.println("Nodecount: " + persistedGraph.getNodeCount());
	    System.out.println("Edgecount: " + persistedGraph.getEdgeCount());
	    assertEquals(graphName1, persistedGraph.getName());
	    assertEquals(userName1, persistedGraph.getUserName());
	    assertEquals(3, persistedGraph.getNodeCount());
	    assertEquals(2, persistedGraph.getEdgeCount());
	    Set<String> nodeNames = new HashSet<String>();
	    nodeNames.add("A");
	    nodeNames.add("B");
	    nodeNames.add("C");
		Node[] nodes = graph.nodes().toArray(Node[]::new);
	    for(int i=0; i<3; i++) {
	    	Node node = nodes[i];
	    	String name = persistedGraph.getNodeName(node);
	    	System.out.println("Node: " + node.getIndex() + ", Name: " + persistedGraph.getNodeName(node));
	    	assertTrue(nodeNames.contains(name));
	    	nodeNames.remove(name);
	    }
		Edge[] edges = persistedGraph.edges().toArray(Edge[]::new);
	    for(int i=0; i<2; i++) {
	    	Edge edge =edges[i];
	    	Double weight = persistedGraph.getEdgeWeight(edge);
	    	if(weight == 5) {
	    		assertEquals("A", persistedGraph.getNodeName(edge.getSourceNode()));
	    	    assertEquals("B", persistedGraph.getNodeName(edge.getTargetNode()));
	    	}
	    	else if(weight == 2.5) {
	    		assertEquals("B", persistedGraph.getNodeName(edge.getSourceNode()));
	    	    assertEquals("C", persistedGraph.getNodeName(edge.getTargetNode()));
	    	}
	    	else {
	    		throw new IllegalStateException("Invalid Node Weight");
	    	}
	    }
	    assertEquals(1, persistedGraph.getTypes().size());
	    assertTrue(persistedGraph.getTypes().contains(GraphType.DIRECTED));
	    System.out.println("Types: " + graph.getTypes());
	    
	    queryResults = database.getGraphs(invalidGraphName);
	    
		assertEquals(0, queryResults.size());
	}
	
}