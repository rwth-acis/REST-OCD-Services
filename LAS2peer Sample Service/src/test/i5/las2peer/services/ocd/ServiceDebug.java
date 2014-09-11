package i5.las2peer.services.ocd;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputAdapter;
import i5.las2peer.services.ocd.adapters.graphOutput.GraphOutputFormat;
import i5.las2peer.services.ocd.algorithms.AlgorithmLog;
import i5.las2peer.services.ocd.algorithms.AlgorithmType;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.benchmarks.BenchmarkLog;
import i5.las2peer.services.ocd.benchmarks.BenchmarkType;
import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmarkModel;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverId;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphId;
import i5.las2peer.services.ocd.testsUtil.OcdTestGraphFactory;
import i5.las2peer.services.ocd.utils.Error;
import i5.las2peer.services.ocd.utils.RequestHandler;
import i5.las2peer.services.ocd.utils.ThreadHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.xml.parsers.ParserConfigurationException;

import jdk.nashorn.internal.ir.annotations.Ignore;

import org.junit.Test;
import org.la4j.matrix.sparse.CCSMatrix;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

public class ServiceDebug {

	private final String username = "testuser";
	private RequestHandler requestHandler = new RequestHandler();
	private ThreadHandler threadHandler = new ThreadHandler();
	
	public ServiceDebug() {
		RequestHandler.setPersistenceUnit("test");
	}
	
	public void createGraph(CustomGraph graph) throws AdapterException, FileNotFoundException, ParserConfigurationException {
		graph.setUserName(username);
		EntityManager em = requestHandler.getEntityManager();
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
		EntityManager em = requestHandler.getEntityManager();
		TypedQuery<CustomGraph> query = em.createQuery("Select g from CustomGraph g where g.userName = :username", CustomGraph.class);
		query.setParameter("username", username);
		List<CustomGraph> queryResults = query.getResultList();
		em.close();
		System.out.println(requestHandler.getGraphIds(queryResults));
	}
	
	private String getGraph(String graphIdStr, String outputFormatIdStr) {
		try {
			EntityManager em = requestHandler.getEntityManager();
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
		    	format = GraphOutputFormat.lookupFormat(formatId);
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
	
	public CoverId runAlgorithm(String algorithmTypeStr, String graphIdStr, 
		String componentNodeCountFilterStr, Map<String, String> parameters) throws IOException, SAXException, ParserConfigurationException {
		int componentNodeCountFilter;
		long graphId;
		AlgorithmType algorithmType;
		graphId = Long.parseLong(graphIdStr);
		componentNodeCountFilter = Integer.parseInt(componentNodeCountFilterStr);
		algorithmType = AlgorithmType.valueOf(algorithmTypeStr);
		if(algorithmType == AlgorithmType.UNDEFINED || algorithmType == AlgorithmType.GROUND_TRUTH) {
			throw new IllegalArgumentException();
		}
		OcdAlgorithm algorithm;
		algorithm = algorithmType.getAlgorithmInstance(parameters);
		Cover cover;
    	EntityManager em = requestHandler.getEntityManager();
    	CustomGraphId id = new CustomGraphId(graphId, username);
    	AlgorithmLog log;
    	synchronized(threadHandler) {
	    	EntityTransaction tx = em.getTransaction();
	    	CustomGraph graph;
			tx.begin();
			graph = em.find(CustomGraph.class, id);
	    	if(graph == null) {
	    		throw new IllegalArgumentException();
	    	}
	    	cover = new Cover(graph, new CCSMatrix(graph.nodeCount(), 0));
	    	log = new AlgorithmLog(algorithmType, parameters, algorithm.compatibleGraphTypes());
	    	cover.setAlgorithm(log);
	    	em.persist(cover);
	    	/*
	    	 * Registers and starts algorithm
	    	 */
			tx.commit();
			em.close();
			threadHandler.runAlgorithm(cover, algorithm, componentNodeCountFilter);
    	}
    	return new CoverId(cover.getId(), new CustomGraphId(cover.getGraph().getId(), cover.getGraph().getUserName()));
    }
	
    public CoverId createBenchmarkCover(String coverNameStr, String graphNameStr,
		BenchmarkType benchmarkType, Map<String, String> parameters) {
		GroundTruthBenchmarkModel benchmark = benchmarkType.getGroundTruthBenchmarkInstance(parameters);
    	EntityManager em = requestHandler.getEntityManager();
    	CustomGraph graph = new CustomGraph();
    	graph.setName(graphNameStr);
    	BenchmarkLog log = new BenchmarkLog(benchmarkType, parameters);
    	graph.setBenchmark(log);
    	Cover cover = new Cover(graph, new CCSMatrix());
    	cover.setName(coverNameStr);
    	synchronized(threadHandler) {
	    	EntityTransaction tx = em.getTransaction();
	    	try {
				tx.begin();
				em.persist(graph);
		    	em.persist(cover);
				tx.commit();
			} catch( RuntimeException e ) {
				if( tx != null && tx.isActive() ) {
					tx.rollback();
				}
				throw e;
			}
			em.close();
			/*
			 * Registers and starts benchmark creation.
			 */
			threadHandler.runGroundTruthBenchmark(cover, benchmark);
    	}
    	return new CoverId(cover.getId(), new CustomGraphId(cover.getGraph().getId(), cover.getGraph().getUserName()));
    }
	
    @Ignore
	@Test
	public void debugAlgoTest() throws AdapterException, ParserConfigurationException, InterruptedException, IOException, SAXException {
    	System.out.println("DEBUG ALGO TEST");
		createTestGraphs();
		CoverId cId = runAlgorithm("SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM", "1", "0", new HashMap<String, String>());
		System.out.println("CoverId: " + cId);
		Thread.sleep(5000);
		Cover cover;
		EntityManager em = requestHandler.getEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			cover = em.find(Cover.class, cId);
			System.out.println("Final cover: ");
			System.out.println(cover.toString());
			tx.commit();
		} catch( RuntimeException e ) {
			if( tx != null && tx.isActive() ) {
				tx.rollback();
			}
			throw e;
		}
	}
    
    @Test
    public void debugBenchmarkTest() throws InterruptedException {
    	System.out.println("DEBUG BENCHMARK TEST");
    	CoverId cId = this.createBenchmarkCover("newman01cover", "newman01graph", BenchmarkType.NEWMAN, new HashMap<String, String>());
		EntityManager em = requestHandler.getEntityManager();
		EntityTransaction tx = em.getTransaction();
		Cover cover;
		Thread.sleep(20000);
		try {
			tx.begin();
			cover = em.find(Cover.class, cId);
			System.out.println("Final cover: ");
			System.out.println(cover.toString());
			tx.commit();
		} catch( RuntimeException e ) {
			if( tx != null && tx.isActive() ) {
				tx.rollback();
			}
			throw e;
		}
    }
}
