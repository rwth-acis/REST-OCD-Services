package i5.las2peer.services.ocd.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricType;
import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.utils.ThreadHandler;

import java.awt.Color;
import java.util.*;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;


//@Ignore
public class CoverDatabaseTest {

	private static final String userName = "coverPersistenceUser";
	private static final String graphName = "coverPersistenceGraph";
	private static final String coverName = "coverPersistenceCover";
	private static final String invalidCoverName = "invalidCoverName";
	private static Database database;
	
	@BeforeAll
	public static void clearDatabase() {
		database = new Database(true);
	}
	
	@AfterAll
	public static void deleteDatabase() {
		database.deleteDatabase();
	}
	
	@Test
	public void testPersist() {
		CustomGraph graph = new CustomGraph();
		graph.setUserName(userName);
		graph.setName(graphName);
		Node nodeA = graph.addNode("nodeA");
		Node nodeB = graph.addNode("nodeB");
		Node nodeC = graph.addNode("nodeC");
		graph.setNodeName(nodeA, "A");
		graph.setNodeName(nodeB, "B");
		graph.setNodeName(nodeC, "C");
		Edge edgeAB = graph.addEdge(UUID.randomUUID().toString(), nodeA, nodeB);
		graph.setEdgeWeight(edgeAB, 5);
		Edge edgeBC = graph.addEdge(UUID.randomUUID().toString(), nodeB, nodeC);
		graph.setEdgeWeight(edgeBC, 2.5);
		
		String Gkey = database.storeGraph(graph);
		System.out.println("graph key: " + graph.getKey());

		Matrix memberships = new CCSMatrix(3, 2);
		memberships.set(0, 0, 1);
		memberships.set(1, 0, 0.5);
		memberships.set(1, 1, 0.5);
		memberships.set(2, 1, 1);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("param1", "val1");
		params.put("param2", "val2");
		CoverCreationLog algo = new CoverCreationLog(CoverCreationType.UNDEFINED, params, new HashSet<GraphType>());
		Cover cover = new Cover(graph, memberships);
		cover.setCreationMethod(algo);
		cover.setName(coverName);
		cover.setCommunityColor(1, Color.BLUE);
		cover.setCommunityName(1, "Community1");
		OcdMetricLog metric = new OcdMetricLog(OcdMetricType.EXECUTION_TIME, 3.55, params, cover);
		cover.addMetric(metric);
		
		database.storeCover(cover);
		
		List<Cover> queryResults = database.getCoversByName(coverName, cover.getGraph());
		assertEquals(1, queryResults.size());
		Cover persistedCover = queryResults.get(0);
		persistedCover.toString();
		System.out.println("Name: " + persistedCover.getName());
		System.out.println("Community Count: " + persistedCover.communityCount());
		System.out.println("Algo: " + persistedCover.getCreationMethod().getType().toString());
		System.out.println("Metrics Count: " + persistedCover.getMetrics().size());
		for(int i=0; i<cover.communityCount(); i++) {
			System.out.println("Com: " + i);
			System.out.println("Name cov: " + cover.getCommunityName(i));
			System.out.println("Name covP: " + persistedCover.getCommunityName(i));
			System.out.println("Color cov: " + cover.getCommunityColor(i));
			System.out.println("Color covP: " + persistedCover.getCommunityColor(i));
		}
		assertEquals(coverName, persistedCover.getName());
		assertEquals(graphName, persistedCover.getGraph().getName());
		assertEquals(cover.communityCount(), persistedCover.communityCount());
		for(int i=0; i<cover.communityCount(); i++) {
			assertEquals(cover.getCommunityColor(i), persistedCover.getCommunityColor(i));
			assertEquals(cover.getCommunityName(i), persistedCover.getCommunityName(i));
			assertEquals(cover.getCommunitySize(i), persistedCover.getCommunitySize(i));
		}
		assertEquals(cover.getCreationMethod().getType(), persistedCover.getCreationMethod().getType());
		assertEquals(cover.getMetrics().size(), persistedCover.getMetrics().size());
		for(int i=0; i<cover.getMetrics().size(); i++) {
			assertEquals(cover.getMetrics().get(i).getType(), persistedCover.getMetrics().get(i).getType());
			assertEquals(cover.getMetrics().get(i).getValue(), persistedCover.getMetrics().get(i).getValue(), 0);
		}

		queryResults = database.getCoversByName(invalidCoverName, graph);
		assertEquals(0, queryResults.size());
		try {
			database.deleteGraph(userName, Gkey, new ThreadHandler());
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
}
