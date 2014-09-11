package i5.las2peer.services.ocd.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import y.base.Edge;
import y.base.Node;

public class CustomGraphPersistenceTest {

	private static final String userName1 = "testUser1";
	private static final String graphName1 = "persistenceTestGraph1";
	private static final String invalidGraphName = "invalidGraphName";
	
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("test");
	
	@Test
	public void testPersist() {
		EntityManager em = emf.createEntityManager();
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
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.persist(graph);
			tx.commit();
		} catch( RuntimeException ex ) {
			if( tx != null && tx.isActive() ) tx.rollback();
			throw ex;
		}
		em.close();
		em = emf.createEntityManager();
		TypedQuery<CustomGraph> query = em.createQuery("Select g from CustomGraph g where g.name = :name", CustomGraph.class);
		query.setParameter("name", graphName1);
		List<CustomGraph> queryResults = query.getResultList();
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
		query = em.createQuery("Select graph from CustomGraph graph where graph.name = :name", CustomGraph.class);
		query.setParameter("name", invalidGraphName);
		queryResults = query.getResultList();
		assertEquals(0, queryResults.size());
	}
	
	/*
	 * Tests whether two instances of the same object obtained by a single entity manager
	 * are indeed identical (i.e. the comparison == results in TRUE).
	 */
	@Ignore
	@Test
	public void testIdentity() {
		EntityManager em = emf.createEntityManager();
		CustomGraph graph = new CustomGraph();
		graph.setUserName(userName1);
		graph.setName(graphName1);
		Node nodeA = graph.createNode();
		Node nodeB = graph.createNode();
		Node nodeC = graph.createNode();
		graph.setNodeName(nodeA, "A");
		graph.setNodeName(nodeB, "B");
		graph.setNodeName(nodeC, "C");
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.persist(graph);
			tx.commit();
		} catch( RuntimeException ex ) {
			if( tx != null && tx.isActive() ) tx.rollback();
			throw ex;
		}
		em.close();
		em = emf.createEntityManager();
		TypedQuery<CustomGraph> query = em.createQuery("Select g from CustomGraph g where g.name = :name", CustomGraph.class);
		query.setParameter("name", graphName1);
		List<CustomGraph> queryResults1 = query.getResultList();
		assertEquals(1, queryResults1.size());
	    CustomGraph persistedGraph1 = queryResults1.get(0);
	    List<CustomGraph> queryResults2 = query.getResultList();
		assertEquals(1, queryResults2.size());
		CustomGraph persistedGraph2 = queryResults2.get(0);
		assertTrue(persistedGraph1 == persistedGraph2);
		Node node = persistedGraph1.getNodeArray()[0];
		String name1 = persistedGraph1.getNodeName(node);
		String name2 = persistedGraph2.getNodeName(node);
		System.out.println("name1: " + name1);
		System.out.println("name2: " + name2);
		assertEquals(name1, name2);
	}
	
	@After
	public void after() {
		emf.close();
	}
	
}