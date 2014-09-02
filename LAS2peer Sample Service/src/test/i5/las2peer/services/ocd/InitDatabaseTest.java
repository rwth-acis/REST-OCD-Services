package i5.las2peer.services.ocd;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.testsUtil.OcdTestGraphFactory;
import i5.las2peer.services.ocd.utils.RequestHandler;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;

public class InitDatabaseTest {

	private EntityManagerFactory emf = Persistence.createEntityManagerFactory("test");
	private final String username = "User";
	
	public void createGraph(CustomGraph graph) throws AdapterException, FileNotFoundException, ParserConfigurationException {
		graph.setUserName(username);
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			em.persist(graph);
			tx.commit();
		} catch( RuntimeException e ) {
			if( tx != null && tx.isActive() ) {
				tx.rollback();
			}
			throw e;
		}
		em.close();
	}
	
	@Test
	public void createTestGraphs() throws AdapterException, FileNotFoundException, ParserConfigurationException {
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		createGraph(graph);
		graph = OcdTestGraphFactory.getSawmillGraph();
		createGraph(graph);
		graph = OcdTestGraphFactory.getDolphinsGraph();
		createGraph(graph);
		graph = OcdTestGraphFactory.getDirectedAperiodicTwoCommunitiesGraph();
		createGraph(graph);
		DecimalFormat df = new DecimalFormat("00");
		for(int i=0; i<10; i++) {
			graph = OcdTestGraphFactory.getMiniServiceTestGraph();
			graph.setName(graph.getName() + " " + df.format(i));
			createGraph(graph);
		}
	}

}
