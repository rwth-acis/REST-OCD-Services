package i5.las2peer.services.ocd.utils;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;


public class DatabaseMethodTest {

	private static Database database;

	@BeforeAll
	public static void setupTestDatabase() {
		database = new Database(true);

	}
	@BeforeEach
	public void clearDatabase() {
		database.deleteDatabase();
		database.init();
	}
	
	@AfterAll
	public static void deleteDatabase() {
		database.deleteDatabase();
	}

	public CustomGraph getTestGraph() {
		CustomGraph graph = null;
		try {
			graph = OcdTestGraphFactory.getDolphinsGraph();
		} catch (FileNotFoundException | AdapterException e) {
			e.printStackTrace();
		}
		return graph;
	}

	@Test
	public void getGraph() {
		try {
			CustomGraph graph = OcdTestGraphFactory.getDolphinsGraph();
			graph.setUserName("eve");
			database.storeGraph(graph);
			String graphKey = graph.getKey();

			CustomGraph resultGraph = database.getGraph("eve", graphKey);
			assertNotNull(resultGraph);
			assertEquals(graphKey, resultGraph.getKey());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getGraphNotFound() {
		CustomGraph graph = database.getGraph("eve", "0");
		if(graph == null) {
			System.out.println("graph is null in getGraphNotFound");
		}
		else {System.out.println(graph.String());}
		assertNull(graph);
	}

	@Test
	public void storeGraph() {

		CustomGraph graph = getTestGraph();
		graph.setUserName("testUser231");
		graph.setName("testGraphName231");

		database.storeGraph(graph);
		String graphKey = graph.getKey();
		CustomGraph resultGraph = database.getGraph("testUser231", graphKey);
		assertEquals(graph.getName(), resultGraph.getName());
		assertEquals(graph.getUserName(), resultGraph.getUserName());
		assertEquals(graph.getNodeCount(), resultGraph.getNodeCount());
		assertEquals(graph.getEdgeCount(), resultGraph.getEdgeCount());
	}

	@Test
	public void deleteGraph() {
		CustomGraph graph1 = null;
		CustomGraph graph2 = null;
		Cover cover = null;
		try {
			graph1 = OcdTestGraphFactory.getSawmillGraph();
			graph1.setUserName("eve");
			graph2 = getTestGraph();
			graph2.setUserName("eve");
			cover = new Cover(graph1);

		} catch (Exception e) {
			e.printStackTrace();
		}

		database.storeGraph(graph1);
		database.storeGraph(graph2);
		database.storeCover(cover);

		String graphKey = graph1.getKey();
		try {
			database.deleteGraph("eve", graphKey, new ThreadHandler());
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<CustomGraph> queryResults = database.getGraphs("eve");
		assertEquals(1, queryResults.size());
	}

	@Test
	public void getCover() {

		try {
			CustomGraph graph = null;
			Cover cover = null;
			try {
				graph = OcdTestGraphFactory.getSawmillGraph();
				graph.setUserName("eve");				
				cover = new Cover(graph);
			} catch (Exception e) {
				e.printStackTrace();
			}
			database.storeGraph(graph);
			database.storeCover(cover);
			String graphKey = graph.getKey();
			String coverKey = cover.getKey();
				
			Cover resultCover;
			resultCover = database.getCover("eve", graphKey, coverKey);
			assertNotNull(resultCover);
			assertEquals(coverKey, resultCover.getKey());
			assertEquals(graphKey, resultCover.getGraph().getKey());

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("getcover success");
	}
	
	@Test
	public void getCoverNotFound() {
		Cover cover = database.getCover("eve", "2", "2");
		assertNull(cover);
		System.out.println("getcovernotfound success");
	}
	
	@Test
	public void deleteCover() {

		Cover cover1 = null;
		Cover cover2 = null;
		Cover cover3 = null;
		CustomGraph graph = null;
		try {
			graph = getTestGraph();
			graph.setUserName("eve");
			cover1 = new Cover(graph);
			cover2 = new Cover(graph);
			cover3 = new Cover(graph);
		} catch (Exception e) {
			e.printStackTrace();
		}

		database.storeGraph(graph);
		database.storeCover(cover1);
		database.storeCover(cover2);
		database.storeCover(cover3);
		
		String graphKey = graph.getKey();
		String cover2Key = cover2.getKey();

		try {
			database.deleteCover("eve", graphKey, cover2Key, new ThreadHandler());
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		List<String> covers = database.getAllCoverKeys();
		assertEquals(2, covers.size());
	}
	
	@Test
	public void deleteCoverNotFound() throws Exception {
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			database.deleteCover("eve", "3", "1", new ThreadHandler());
		});

		assertEquals("Cover not found", thrown.getMessage());

	}

}
