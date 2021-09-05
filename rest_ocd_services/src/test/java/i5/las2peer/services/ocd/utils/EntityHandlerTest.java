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

public class EntityHandlerTest {

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private static final String PERSISTENCE_UNIT_NAME = "ocd";
	private static final EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME,
			Collections.singletonMap(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML, "META-INF/persistenceTesting.xml"));
	private EntityHandler entityHandler = new EntityHandler();

	@Before
	public void clearDatabase() {

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

			EntityManager em = entityHandler.getEntityManager();
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			em.persist(graph);
			em.flush();
			tx.commit();
			long graphId = graph.getId();
			em.close();

			CustomGraph resultGraph;
			resultGraph = entityHandler.getGraph("eve", graphId);
			assertNotNull(resultGraph);
			assertEquals(graphId, resultGraph.getId());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getGraphNotFound() {

		CustomGraph graph = entityHandler.getGraph("eve", 2);
		assertNull(graph);
	}

	@Test
	public void storeGraph() {

		CustomGraph graph = getTestGraph();
		graph.setUserName("testUser231");
		graph.setName("testGraphName231");

		entityHandler.storeGraph(graph);
		long graphId = graph.getId();

		CustomGraphId id = new CustomGraphId(graphId, "testUser231");
		CustomGraph resultGraph = null;
		EntityManager em = entityHandler.getEntityManager();
		EntityTransaction tx = em.getTransaction();

		try {
			tx.begin();
			resultGraph = em.find(CustomGraph.class, id);
			tx.commit();
		} catch (RuntimeException e) {
			tx.rollback();
			throw e;
		}
		em.close();

		assertEquals(graph.getName(), resultGraph.getName());
		assertEquals(graph.getUserName(), resultGraph.getUserName());
		assertEquals(graph.nodeCount(), resultGraph.nodeCount());
		assertEquals(graph.edgeCount(), resultGraph.edgeCount());
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

		EntityManager em = entityHandler.getEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();

		em.persist(graph1);
		em.persist(graph2);
		em.persist(cover);

		tx.commit();

		long graphId = graph1.getId();
		try {
			entityHandler.deleteGraph("eve", graphId, new ThreadHandler());
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<CustomGraph> queryResults;
		String queryStr = "SELECT g FROM CustomGraph g WHERE g." + CustomGraph.USER_NAME_FIELD_NAME + " = :username";
		TypedQuery<CustomGraph> query = em.createQuery(queryStr, CustomGraph.class);
		query.setParameter("username", "eve");
		queryResults = query.getResultList();
		em.close();
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

			EntityManager em = entityHandler.getEntityManager();
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			em.persist(graph);
			em.persist(cover);
			tx.commit();
			long graphId = graph.getId();
			long coverId = cover.getId();
						
			Cover resultCover;
			resultCover = entityHandler.getCover("eve", graphId, coverId);
			assertNotNull(resultCover);
			assertEquals(coverId, resultCover.getId());
			assertEquals(graphId, resultCover.getGraph().getId());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getCoverNotFound() {

		Cover cover = entityHandler.getCover("eve", 2, 2);
		assertNull(cover);
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
		
		EntityManager em = entityHandler.getEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.persist(graph);
		em.persist(cover1);
		em.persist(cover2);
		em.persist(cover3);
		tx.commit();

		long graphId = graph.getId();
		long cover2Id = cover2.getId();
		try {
			entityHandler.deleteCover("eve", graphId, cover2Id, new ThreadHandler());
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		List<Cover> queryResults;
		String queryStr = "SELECT c FROM Cover c";
		TypedQuery<Cover> query = em.createQuery(queryStr, Cover.class);		
		queryResults = query.getResultList();
		em.close();
		assertEquals(2, queryResults.size());
	}
	
	@Test
	public void deleteCoverNotFound() throws Exception {
		 
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Cover not found");
		
		entityHandler.deleteCover("eve", 3, 1, new ThreadHandler());
	}

}
