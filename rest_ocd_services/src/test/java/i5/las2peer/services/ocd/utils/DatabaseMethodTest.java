package i5.las2peer.services.ocd.utils;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import i5.las2peer.services.ocd.utils.Database;
import i5.las2peer.services.ocd.utils.DatabaseConfig;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphId;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class DatabaseMethodTest {

	public final ExpectedException exception = ExpectedException.none();
	private final Database database = new Database();

	private EntityHandler entityHandler = new EntityHandler();

	@Before
	public void clearDatabase() {
		DatabaseConfig.setConfigFile(true);
		database.deleteDatabase();
		database.init();
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
		System.out.println("getGraph success");
	}

	@Test
	public void getGraphNotFound() {
		CustomGraph graph = entityHandler.getGraph("eve", 0);
		assertNull(graph);
		System.out.println("Graphnotfound success");
	}

	@Test
	public void storeGraph() {

		CustomGraph graph = getTestGraph();
		graph.setUserName("testUser231");
		graph.setName("testGraphName231");

		database.storeGraph(graph);
		String graphKey = graph.getKey();
		//CustomGraphId id = new CustomGraphId(graphId, "testUser231");
		CustomGraph resultGraph = database.getGraph(graphKey);
		assertEquals(graph.getName(), resultGraph.getName());
		assertEquals(graph.getUserName(), resultGraph.getUserName());
		assertEquals(graph.nodeCount(), resultGraph.nodeCount());
		assertEquals(graph.edgeCount(), resultGraph.edgeCount());
		System.out.println("storeGraph success");
	}

	//@Test
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
			//database.deleteGraph("eve", graphKey, new ThreadHandler());
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<CustomGraph> queryResults = database.getGraphs("eve");
		assertEquals(2, queryResults.size());	//TODO auf 1 ändern nachdem graph1 gelöscht wurde
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

//		try {
//			database.deleteCover("eve", graphId, cover2Id, new ThreadHandler());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//				
//		List<Cover> queryResults;
//		String queryStr = "SELECT c FROM Cover c";
//		TypedQuery<Cover> query = em.createQuery(queryStr, Cover.class);		
//		queryResults = query.getResultList();
//		em.close();
//		assertEquals(2, queryResults.size());
	}
	
	@Test
	public void deleteCoverNotFound() throws Exception {
		 
//		exception.expect(IllegalArgumentException.class);
//		exception.expectMessage("Cover not found");
//		
//		entityHandler.deleteCover("eve", 3, 1, new ThreadHandler());
	}

}
