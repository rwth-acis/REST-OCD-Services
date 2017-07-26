package i5.las2peer.services.ocd.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.base.Node;

public class InvocationHandlerTest {

	private static final String PERSISTENCE_UNIT_NAME = "ocd";
	private static final EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);

	String username;
	String graphName;

	CustomGraph graph;
	List<Node> nodes;

	Cover cover;
	InvocationHandler invocationHandler;

	long graphId;
	long coverId;
	
	@AfterClass
	public static void clearDatabase() {

		EntityManager em = factory.createEntityManager();
		EntityTransaction etx = em.getTransaction();
		etx.begin();
		Query EdgeQuery = em.createQuery("DELETE FROM CustomEdge", CustomGraph.class);
		EdgeQuery.executeUpdate();
		Query memberQuery = em.createQuery("DELETE FROM Community", CustomGraph.class);
		memberQuery.executeUpdate();
		Query NodeQuery = em.createQuery("DELETE FROM CustomNode", CustomGraph.class);
		NodeQuery.executeUpdate();
		Query CoverQuery = em.createQuery("DELETE FROM Cover", CustomGraph.class);
		CoverQuery.executeUpdate();
		Query GraphQuery = em.createQuery("DELETE FROM CustomGraph", CustomGraph.class);
		GraphQuery.executeUpdate();
		etx.commit();
	}

	public void persistEntities() {

		EntityManager em = factory.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.persist(graph);
			em.flush();
			graphId = graph.getId();
			em.persist(cover);
			em.flush();
			coverId = cover.getId();
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null && tx.isActive())
				tx.rollback();
			throw ex;
		}
		em.close();
	}

	@Before
	public void setUp() {

		username = "eve";
		graphName = "testGraphName";

		graph = new CustomGraph();
		graph.setUserName(username);
		graph.setName(graphName);

		nodes = new ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			nodes.add(i, graph.createNode());
			graph.setNodeName(nodes.get(i), String.valueOf(i));
		}

		graph.createEdge(nodes.get(0), nodes.get(1));
		graph.createEdge(nodes.get(1), nodes.get(2));
		graph.createEdge(nodes.get(1), nodes.get(3));
		graph.createEdge(nodes.get(3), nodes.get(2));

		cover = new Cover(graph);

		invocationHandler = new InvocationHandler();
	}

	@Test
	public void getAdjListTest() {

		Node node0 = nodes.get(0);
		Node node1 = nodes.get(1);
		Node node2 = nodes.get(2);
		Node node3 = nodes.get(3);

		assertEquals("0", graph.getNodeName(node0));
		assertEquals("1", graph.getNodeName(node1));
		assertEquals("2", graph.getNodeName(node2));
		assertEquals("3", graph.getNodeName(node3));

		List<List<Integer>> adjList;

		adjList = invocationHandler.getAdjList(graph);
		assertNotNull(adjList);
		assertEquals(4, adjList.size());
		assertEquals(1, adjList.get(0).size());
		assertEquals(2, adjList.get(1).size());
		assertEquals(0, adjList.get(2).size());
		assertEquals(1, adjList.get(3).size());

		assertTrue(adjList.get(0).contains(1));
		assertTrue(adjList.get(1).contains(2));
		assertTrue(adjList.get(1).contains(3));
		assertTrue(adjList.get(3).contains(2));
	}

	@Test
	public void persistenceAdjListTest() {

		clearDatabase();
		persistEntities();

		CustomGraph entityGraph = null;
		EntityHandler entityHandler = new EntityHandler();
		try {
			entityGraph = entityHandler.getGraph("eve", graphId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertNotNull(entityGraph);
		assertEquals(graph.getId(), entityGraph.getId());
		
		List<List<Integer>> adjList;
		adjList = invocationHandler.getAdjList(entityGraph);
		assertNotNull(adjList);

		assertEquals(4, adjList.size());
		assertEquals(1, adjList.get(0).size());
		assertEquals(2, adjList.get(1).size());
		assertEquals(0, adjList.get(2).size());
		assertEquals(1, adjList.get(3).size());
		
		assertTrue(adjList.get(0).contains(1));
		assertTrue(adjList.get(1).contains(2));
		assertTrue(adjList.get(1).contains(3));
		assertTrue(adjList.get(3).contains(2));

	}
	
	@Test
	public void getMemberListTest() {

		Matrix memberships = new Basic2DMatrix(graph.nodeCount(), 3);
		memberships.set(0, 0, 0.7);
		memberships.set(0, 1, 0.0);
		memberships.set(0, 2, 0.0);

		memberships.set(1, 0, 0.7);
		memberships.set(1, 1, 0.8);
		memberships.set(1, 2, 0.0);

		memberships.set(2, 0, 0.0);
		memberships.set(2, 1, 0.5);
		memberships.set(2, 2, 0.0);

		memberships.set(3, 0, 0.0);
		memberships.set(3, 1, 0.4);
		memberships.set(3, 2, 0.6);

		cover.setMemberships(memberships);
		List<List<Integer>> memberLists;

		memberLists = invocationHandler.getCommunityMemberList(cover);
		assertNotNull(memberLists);
		assertEquals(3, memberLists.size());

		assertEquals(2, memberLists.get(0).size());
		assertTrue(memberLists.get(0).contains(0));
		assertTrue(memberLists.get(0).contains(1));
		assertFalse(memberLists.get(0).contains(2));
		assertFalse(memberLists.get(0).contains(3));

		assertFalse(memberLists.get(1).contains(0));
		assertTrue(memberLists.get(1).contains(1));
		assertTrue(memberLists.get(1).contains(2));
		assertTrue(memberLists.get(1).contains(3));
		
		//// after persistence
		
		clearDatabase();
		persistEntities();

		CustomGraph entityGraph = null;
		Cover entityCover = null;
		EntityHandler entityHandler = new EntityHandler();
		try {
			entityGraph = entityHandler.getGraph("eve", graphId);
			entityCover = entityHandler.getCover("eve", coverId, graphId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertNotNull(entityGraph);
		assertEquals(graph.getId(), entityGraph.getId());
		assertNotNull(entityCover);
		assertEquals(cover.getId(), entityCover.getId());
		
		memberLists = invocationHandler.getCommunityMemberList(entityCover);
		assertNotNull(memberLists);
		assertEquals(3, memberLists.size());

		assertEquals(2, memberLists.get(0).size());
		assertTrue(memberLists.get(0).contains(0));
		assertTrue(memberLists.get(0).contains(1));
		assertFalse(memberLists.get(0).contains(2));
		assertFalse(memberLists.get(0).contains(3));

		assertFalse(memberLists.get(1).contains(0));
		assertTrue(memberLists.get(1).contains(1));
		assertTrue(memberLists.get(1).contains(2));
		assertTrue(memberLists.get(1).contains(3));

	}

	

}
