package i5.las2peer.services.ocd;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputAdapter;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputFormat;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.graph.CustomGraphId;
import i5.las2peer.services.ocd.testsUtil.OcdTestGraphFactory;
import i5.las2peer.services.ocd.utils.Error;
import i5.las2peer.services.ocd.utils.RequestHandler;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.DOMException;

public class ServiceDebug {

	private EntityManagerFactory emf = Persistence.createEntityManagerFactory("test");
	private final String username = "testuser";
	private RequestHandler requestHandler = new RequestHandler();
	
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
		System.out.println(requestHandler.getId(graph));
	}
	
	public void createTestGraphs() throws AdapterException, FileNotFoundException, ParserConfigurationException {
		CustomGraph graph = OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph();
		createGraph(graph);
		graph = OcdTestGraphFactory.getMiniServiceTestGraph();
		createGraph(graph);
		graph = OcdTestGraphFactory.getSawmillGraph();
		createGraph(graph);
	}
	
	public void getGraphs() throws ParserConfigurationException, DOMException, AdapterException {
		EntityManager em = emf.createEntityManager();
		TypedQuery<CustomGraph> query = em.createQuery("Select g from CustomGraph g where g.userName = :username", CustomGraph.class);
		query.setParameter("username", username);
		List<CustomGraph> queryResults = query.getResultList();
		em.close();
		System.out.println(requestHandler.getIds(queryResults));
	}
	
	private String getGraph(String graphIdStr, String outputFormatIdStr) {
		try {
			EntityManager em = emf.createEntityManager();
    		long graphId;
    		GraphOutputFormat format;
    		try {
    			graphId = Long.parseLong(graphIdStr);
    		}
    		catch (Exception e) {
				//log.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Graph id is not valid.");
    		}
    		try {
		    	int formatId = Integer.parseInt(outputFormatIdStr);
		    	format = GraphOutputFormat.lookupType(formatId);
    		}
	    	catch (Exception e) {
				//log.log(Level.WARNING, "user: " + username, e);
				return requestHandler.getError(Error.PARAMETER_INVALID, "Specified output format does not exist.");
	    	}
	    	GraphOutputAdapter adapter = format.getAdapterInstance();
	    	Writer writer = new StringWriter();
	    	adapter.setWriter(writer);
	    	
	    	CustomGraphId id = new CustomGraphId(graphId, username);
	    	EntityTransaction tx = em.getTransaction();
	    	CustomGraph graph;
	    	try {
				tx.begin();
				graph = em.find(CustomGraph.class, id);
				tx.commit();
			} catch( RuntimeException e ) {
				if( tx != null && tx.isActive() ) {
					tx.rollback();
				}
				throw e;
			}
			em.close();
			adapter.writeGraph(graph);
	    	return writer.toString();
    	}
    	catch (Exception e) {
    		//log.log(Level.SEVERE, "", e);
    		return requestHandler.getError(Error.INTERNAL, "Internal system error.");
    	}
	}
	
	@Test
	public void testGetGraph() throws AdapterException, FileNotFoundException, ParserConfigurationException {
		createTestGraphs();
		getGraphs();
		System.out.println(getGraph("1", "2"));
		System.out.println(getGraph("2", "2"));
		System.out.println(getGraph("3", "2"));
	}
}
