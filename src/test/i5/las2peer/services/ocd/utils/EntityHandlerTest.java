package i5.las2peer.services.ocd.utils;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class EntityHandlerTest {
	
	private static final String PERSISTENCE_UNIT_NAME = "ocd";
	private static final EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
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
	
	@Test
	public void getGraphTest() {
		
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
	
}
