package i5.las2peer.services.ocd;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithmExecutor;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithmFactory;
import i5.las2peer.services.ocd.algorithms.SpeakerListenerLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmark;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmarkException;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmarkFactory;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.graphs.CoverId;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphId;
import i5.las2peer.services.ocd.graphs.GraphCreationLog;
import i5.las2peer.services.ocd.graphs.GraphCreationType;
import i5.las2peer.services.ocd.metrics.OcdMetricFactory;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricLogId;
import i5.las2peer.services.ocd.metrics.OcdMetricType;
import i5.las2peer.services.ocd.metrics.StatisticalMeasure;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import i5.las2peer.services.ocd.utils.RequestHandler;
import i5.las2peer.services.ocd.utils.ThreadHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Ignore;
import org.junit.Test;
import org.la4j.matrix.sparse.CCSMatrix;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

public class IntegrationTest {

	private final String username = "testuser";
	private RequestHandler requestHandler = new RequestHandler();
	private ThreadHandler threadHandler = new ThreadHandler();
	private OcdBenchmarkFactory benchmarkFactory = new OcdBenchmarkFactory();
	private OcdAlgorithmFactory algorithmFactory = new OcdAlgorithmFactory();
	private OcdMetricFactory metricFactory = new OcdMetricFactory();
	
	public IntegrationTest() {
		RequestHandler.setPersistenceUnit("test");
	}
	
	public CustomGraphId createGraph(CustomGraph graph) throws AdapterException, FileNotFoundException, ParserConfigurationException {
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
		return new CustomGraphId(graph.getId(), username);
	}
	
	public void getGraphs() throws ParserConfigurationException, DOMException, AdapterException {
		EntityManager em = requestHandler.getEntityManager();
		TypedQuery<CustomGraph> query = em.createQuery("Select g from CustomGraph g where g.userName = :username", CustomGraph.class);
		query.setParameter("username", username);
		List<CustomGraph> queryResults = query.getResultList();
		em.close();
		System.out.println(requestHandler.writeGraphIds(queryResults));
	}
	
	public CoverId runAlgorithm(String algorithmTypeStr, CustomGraphId gId, 
		String componentNodeCountFilterStr, Map<String, String> parameters) throws IOException, SAXException, ParserConfigurationException, InstantiationException, IllegalAccessException {
		int componentNodeCountFilter;
		CoverCreationType algorithmType;
		componentNodeCountFilter = Integer.parseInt(componentNodeCountFilterStr);
		algorithmType = CoverCreationType.valueOf(algorithmTypeStr);
		if(!algorithmFactory.isInstantiatable(algorithmType)) {
			throw new IllegalArgumentException();
		}
		OcdAlgorithm algorithm;
		algorithm = algorithmFactory.getInstance(algorithmType, parameters);
		Cover cover;
    	EntityManager em = requestHandler.getEntityManager();
    	CoverCreationLog log;
    	synchronized(threadHandler) {
	    	EntityTransaction tx = em.getTransaction();
	    	CustomGraph graph;
			tx.begin();
			graph = em.find(CustomGraph.class, gId);
	    	if(graph == null) {
	    		throw new IllegalArgumentException();
	    	}
	    	cover = new Cover(graph, new CCSMatrix(graph.nodeCount(), 0));
	    	log = new CoverCreationLog(algorithmType, parameters, algorithm.compatibleGraphTypes());
	    	cover.setCreationMethod(log);
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
		GraphCreationType benchmarkType, Map<String, String> parameters) throws InstantiationException, IllegalAccessException {
		GroundTruthBenchmark benchmark = (GroundTruthBenchmark)benchmarkFactory.getInstance(benchmarkType, parameters);
    	EntityManager em = requestHandler.getEntityManager();
    	CustomGraph graph = new CustomGraph();
    	graph.setName(graphNameStr);
    	GraphCreationLog log = new GraphCreationLog(benchmarkType, parameters);
    	graph.setCreationMethod(log);
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
    
    public OcdMetricLogId runStatisticalMeasure(CoverId cId, OcdMetricType metricType, Map<String, String> parameters) throws InstantiationException, IllegalAccessException
    {
		StatisticalMeasure metric;
    	metric = (StatisticalMeasure)metricFactory.getInstance(metricType, parameters);
		EntityManager em = requestHandler.getEntityManager();
		/*
		 * Finds cover
		 */
    	OcdMetricLog log;
    	synchronized(threadHandler) {
			EntityTransaction tx = em.getTransaction();
			Cover cover;
	    	try {
				tx.begin();
				cover = em.find(Cover.class, cId);
		    	log = new OcdMetricLog(metricType, 0, parameters, cover);
		    	log.setStatus(ExecutionStatus.WAITING);
		    	cover.addMetric(log);
		    	System.out.println(cover.getMetrics().size());
				tx.commit();
			}
	    	catch( RuntimeException e ) {
				if( tx != null && tx.isActive() ) {
					tx.rollback();
				}
				throw e;
			}
	    	threadHandler.runStatisticalMeasure(log, metric, cover);
    	}
    	return new OcdMetricLogId(log.getId(), cId);
    }
	
    @Ignore
	@Test
	public void debugAlgoTest() throws AdapterException, ParserConfigurationException, InterruptedException, IOException, SAXException, InstantiationException, IllegalAccessException {
    	System.out.println("DEBUG ALGO TEST");
		CustomGraphId gId = createGraph(OcdTestGraphFactory.getAperiodicTwoCommunitiesGraph());
		CoverId cId = runAlgorithm("SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM", gId, "0", new HashMap<String, String>());
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
		em.close();
	}
    
    @Ignore
    @Test
    public void debugBenchmarkTest() throws InterruptedException, InstantiationException, IllegalAccessException {
    	System.out.println("DEBUG BENCHMARK TEST");
    	CoverId cId = this.createBenchmarkCover("lfr01cover", "lfr01graph", GraphCreationType.LFR, new HashMap<String, String>());
		EntityManager em = requestHandler.getEntityManager();
		EntityTransaction tx = em.getTransaction();
		Cover cover;
		Thread.sleep(3000);
		synchronized(threadHandler) {
			threadHandler.interruptBenchmark(cId.getGraphId());
			System.out.println("Interrupted");
		}
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
		em.close();
    }
    
    @Test
    public void debugStatisticalMeasureTest() throws InterruptedException, InstantiationException, IllegalAccessException, OcdBenchmarkException, OcdAlgorithmException, AdapterException, FileNotFoundException, ParserConfigurationException {
    	System.out.println("DEBUG STATISTICAL MEASURE TEST");
    	CustomGraphId gId = createGraph(OcdTestGraphFactory.getSawmillGraph());	
		EntityManager em = requestHandler.getEntityManager();
		EntityTransaction tx = em.getTransaction();
		CustomGraph graph;
		try {
			tx.begin();
			graph = em.find(CustomGraph.class, gId);
			tx.commit();
		} catch( RuntimeException e ) {
			if( tx != null && tx.isActive() ) {
				tx.rollback();
			}
			throw e;
		}
		System.out.println("GraphId: " + graph.getId());
		tx = em.getTransaction();
		OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
		Cover cover = executor.execute(graph, new SpeakerListenerLabelPropagationAlgorithm(), 0);
		try {
			tx.begin();
			em.persist(cover);
			tx.commit();
		} catch( RuntimeException e ) {
			if( tx != null && tx.isActive() ) {
				tx.rollback();
			}
			throw e;
		}
		CoverId coverId = new CoverId(cover.getId(), new CustomGraphId(cover.getGraph().getId(), username));
		System.out.println("CoverId: " + cover.getId());
		OcdMetricLogId logId = this.runStatisticalMeasure(coverId, OcdMetricType.EXTENDED_MODULARITY, new HashMap<String, String>());
		System.out.println("LogID: " + logId.getId());
		Thread.sleep(3000);
		em.close();
		em = requestHandler.getEntityManager();
		tx = em.getTransaction();
		try {
			tx.begin();
			Cover cov = em.find(Cover.class, coverId);
			System.out.println("Final cover: ");
			System.out.println(cov.toString());
			System.out.println("Metric Count: " + cov.getMetrics().size());
			System.out.println("Metric Name: " + cov.getMetrics().get(0).getType().name());
			System.out.println("CoverId: " + cov.getId());
			tx.commit();
		} catch( RuntimeException e ) {
			if( tx != null && tx.isActive() ) {
				tx.rollback();
			}
			throw e;
		}
		em.close();
    }
}
