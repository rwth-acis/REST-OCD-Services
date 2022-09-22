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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.arangodb.ArangoCursor;

import y.base.Edge;
import y.base.Node;

public class CustomGraphDatabaseTest {

	private static final String userName1 = "testUser1";
	private static final String graphName1 = "persistenceTestGraph1";
	private static final String invalidGraphName = "invalidGraphName";
	private static Database database;
	
	@BeforeClass
	public static void clearDatabase() {
		DatabaseConfig.setConfigFile(true);
		database = new Database();
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
		Node nodeA = graph.createNode();
		Node nodeB = graph.createNode();
		Node nodeC = graph.createNode();
		graph.setNodeName(nodeA, "A");
		graph.setNodeName(nodeB, "B");
		graph.setNodeName(nodeC, "C");
		Edge edgeAB = graph.createEdge(nodeA, nodeB);
		graph.setEdgeWeight(edgeAB, 5);
		Edge edgeBC = graph.createEdge(nodeB, nodeC);
		graph.setEdgeWeight(edgeBC, 2.5);
		graph.addType(GraphType.DIRECTED);
		
		database.storeGraph(graph);

		List<CustomGraph> queryResults = database.getGraphsbyName(graphName1);
		
		assertEquals(1, queryResults.size());
	    CustomGraph persistedGraph = queryResults.get(0);
	    assertNotNull(persistedGraph);
	    System.out.println("Username: " + persistedGraph.getUserName());
	    System.out.println("Graphname: " + persistedGraph.getName());
	    System.out.println("Nodecount: " + persistedGraph.nodeCount());
	    System.out.println("Edgecount: " + persistedGraph.edgeCount());
	    assertEquals(graphName1, persistedGraph.getName());
	    assertEquals(userName1, persistedGraph.getUserName());
	    assertEquals(3, persistedGraph.nodeCount());
	    assertEquals(2, persistedGraph.edgeCount());
	    Set<String> nodeNames = new HashSet<String>();
	    nodeNames.add("A");
	    nodeNames.add("B");
	    nodeNames.add("C");
	    for(int i=0; i<3; i++) {
	    	Node node = persistedGraph.getNodeArray()[i];
	    	String name = persistedGraph.getNodeName(node);
	    	System.out.println("Node: " + node.index() + ", Name: " + persistedGraph.getNodeName(node));
	    	assertTrue(nodeNames.contains(name));
	    	nodeNames.remove(name);
	    }
	    for(int i=0; i<2; i++) {
	    	Edge edge = persistedGraph.getEdgeArray()[i];
	    	Double weight = persistedGraph.getEdgeWeight(edge);
	    	if(weight == 5) {
	    		assertEquals("A", persistedGraph.getNodeName(edge.source()));
	    	    assertEquals("B", persistedGraph.getNodeName(edge.target()));
	    	}
	    	else if(weight == 2.5) {
	    		assertEquals("B", persistedGraph.getNodeName(edge.source()));
	    	    assertEquals("C", persistedGraph.getNodeName(edge.target()));
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