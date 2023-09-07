package i5.las2peer.services.ocd.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



import org.junit.Before;
import org.junit.Test;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.graphstream.graph.Node;

public class InvocationHandlerTest {

	//private static final String PERSISTENCE_UNIT_NAME = "ocd";
	//private static final EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);

	String username;
	String graphName;

	CustomGraph graph;
	List<Node> nodes;

	Cover cover;
	InvocationHandler invocationHandler;

	long graphId;
	long coverId;
		
	@Before
	public void setUp() {

		username = "eve";
		graphName = "testGraphName";

		graph = new CustomGraph();
		graph.setUserName(username);
		graph.setName(graphName);

		nodes = new ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			nodes.add(i, graph.addNode(Integer.toString(i)));
			graph.setNodeName(nodes.get(i), String.valueOf(i + 1));
		}

		graph.addEdge(UUID.randomUUID().toString(), nodes.get(0), nodes.get(1));
		graph.addEdge(UUID.randomUUID().toString(), nodes.get(1), nodes.get(2));
		graph.addEdge(UUID.randomUUID().toString(), nodes.get(1), nodes.get(3));
		graph.addEdge(UUID.randomUUID().toString(), nodes.get(3), nodes.get(2));

		cover = new Cover(graph);

		invocationHandler = new InvocationHandler();
	}

	@Test
	public void getAdjListTest() {

		Node node1 = nodes.get(0);
		Node node2 = nodes.get(1);
		Node node3 = nodes.get(2);
		Node node4 = nodes.get(3);

		assertEquals("1", graph.getNodeName(node1));
		assertEquals("2", graph.getNodeName(node2));
		assertEquals("3", graph.getNodeName(node3));
		assertEquals("4", graph.getNodeName(node4));

		List<List<Integer>> adjList;

		adjList = invocationHandler.getAdjList(graph);
		assertNotNull(adjList);
		assertEquals(5, adjList.size());
		assertEquals(1, adjList.get(1).size());
		assertEquals(2, adjList.get(2).size());
		assertEquals(0, adjList.get(3).size());
		assertEquals(1, adjList.get(4).size());

		assertTrue(adjList.get(1).contains(2));
		assertTrue(adjList.get(2).contains(3));
		assertTrue(adjList.get(2).contains(4));
		assertTrue(adjList.get(4).contains(3));
	}
	
	@Test
	public void getMemberListTest() {

		Matrix memberships = new Basic2DMatrix(graph.getNodeCount(), 3);
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
		assertTrue(memberLists.get(0).contains(1));
		assertTrue(memberLists.get(0).contains(2));
		assertFalse(memberLists.get(0).contains(3));
		assertFalse(memberLists.get(0).contains(4));

		assertFalse(memberLists.get(1).contains(1));
		assertTrue(memberLists.get(1).contains(2));
		assertTrue(memberLists.get(1).contains(3));
		assertTrue(memberLists.get(1).contains(4));
				
	}

	

}
