package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmark;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.data.CentralityMapId;
import i5.las2peer.services.ocd.centrality.simulations.CentralitySimulation;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverCreationLog;
import i5.las2peer.services.ocd.graphs.CoverId;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphId;
import i5.las2peer.services.ocd.graphs.GraphCreationLog;
import i5.las2peer.services.ocd.metrics.KnowledgeDrivenMeasure;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricLogId;
import i5.las2peer.services.ocd.metrics.StatisticalMeasure;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Handles the execution and synchronization of threads for running OCD algorithms, centrality algorithms, simulations, 
 * benchmarks and metrics.
 * @author Sebastian, Tobias
 *
 */
public class ThreadHandler {

	/**
	 * Defines how many threads are allowed to run in parallel.
	 */
	private static final int THREAD_POOL_SIZE = 4;
	
	/**
	 * Mapping from the id of a cover in calculation to the future of the algorithm calculating it.
	 */
	private static Map<CoverId, Future<CoverCreationLog>> algorithms = new HashMap<CoverId, Future<CoverCreationLog>>();
	
	/**
	 * Mapping from the id of a CentralityMap in calculation to the future of the algorithm calculating it.
	 */
	private static Map<CentralityMapId, Future<CentralityCreationLog>> centralityAlgorithms = new HashMap<CentralityMapId, Future<CentralityCreationLog>>();
	
	/**
	 * Mapping from the id of a metric being calculated to the future of its execution.
	 */
	private static Map<OcdMetricLogId, Future<OcdMetricLog>> metrics = new HashMap<OcdMetricLogId, Future<OcdMetricLog>>();
	
	/**
	 * Mapping from the id of a graph in calculation to the future of the benchmark calculating it.
	 */
	private static Map<CustomGraphId, Future<GraphCreationLog>> benchmarks = new HashMap<CustomGraphId, Future<GraphCreationLog>>();
	
	/**
	 * A thread pool for the execution of algorithm, benchmark and metric runnables.
	 */
	private static ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	
	/**
	 * Creates a new instance.
	 */
	private RequestHandler requestHandler = new RequestHandler();
	
	/**
	 * Creates a new instance.
	 */
	private EntityHandler entityHandler = new EntityHandler();
	/**
	 * Runs an algorithm.
	 * @param cover The cover that is already persisted but not holding any valid information aside the graph and id.
	 * @param algorithm The algorithm to calculate the cover with.
	 * @param componentNodeCountFilter The node count filter used by the OcdAlgorithmExecutor.
	 */
	public void runAlgorithm(Cover cover, OcdAlgorithm algorithm, int componentNodeCountFilter) {
		CustomGraphId gId = new CustomGraphId(cover.getGraph().getId(), cover.getGraph().getUserName());
		CoverId coverId = new CoverId(cover.getId(), gId);
		AlgorithmRunnable runnable = new AlgorithmRunnable(cover, algorithm, componentNodeCountFilter, this);
		CoverCreationLog log = cover.getCreationMethod();
		synchronized (algorithms) {
			Future<CoverCreationLog> future = executor.<CoverCreationLog>submit(runnable, log);
			algorithms.put(coverId, future);
		}
	}
	
	/**
	 * Runs a CentralityAlgorithm.
	 * @param map The centrality map that is already persisted but not holding any valid information aside the graph and id.
	 * @param algorithm The algorithm to calculate the centrality values with.
	 */
	public void runCentralityAlgorithm(CentralityMap map, CentralityAlgorithm algorithm) {
		CustomGraphId gId = new CustomGraphId(map.getGraph().getId(), map.getGraph().getUserName());
		CentralityMapId mapId = new CentralityMapId(map.getId(), gId);
		CentralityAlgorithmRunnable runnable = new CentralityAlgorithmRunnable(map, algorithm, this);
		CentralityCreationLog log = map.getCreationMethod();
		synchronized (centralityAlgorithms) {
			Future<CentralityCreationLog> future = executor.<CentralityCreationLog>submit(runnable, log);
			centralityAlgorithms.put(mapId, future);
		}
	}
	
	/**
	 * Runs a CentralitySimulation.
	 * @param map The centrality map that is already persisted but not holding any valid information aside the graph and id.
	 * @param simulation The CentralitySimulation to calculate the centrality values with
	 */
	public void runCentralitySimulation(CentralityMap map, CentralitySimulation simulation) {
		CustomGraphId gId = new CustomGraphId(map.getGraph().getId(), map.getGraph().getUserName());
		CentralityMapId mapId = new CentralityMapId(map.getId(), gId);
		CentralitySimulationRunnable runnable = new CentralitySimulationRunnable(map, simulation, this);
		CentralityCreationLog log = map.getCreationMethod();
		synchronized (centralityAlgorithms) {
			Future<CentralityCreationLog> future = executor.<CentralityCreationLog>submit(runnable, log);
			centralityAlgorithms.put(mapId, future);
		}
	}
	
	/**
	 * Runs a ground truth benchmark.
	 * @param cover The id for the reserved, prepersisted ground truth cover.
	 * @param benchmark The benchmark model to calculate the ground truth cover with.
	 */
	public void runGroundTruthBenchmark(Cover cover, GroundTruthBenchmark benchmark) {
		CustomGraphId gId = new CustomGraphId(cover.getGraph().getId(), cover.getGraph().getUserName());
		CoverId coverId = new CoverId(cover.getId(), gId);
		GroundTruthBenchmarkRunnable runnable = new GroundTruthBenchmarkRunnable(coverId, benchmark, this);
		GraphCreationLog log = cover.getGraph().getCreationMethod();
		synchronized (benchmarks) {
			Future<GraphCreationLog> future = executor.<GraphCreationLog>submit(runnable, log);
			benchmarks.put(gId, future);
		}
	}
	
	/**
	 * Runs a statistical measure.
	 * @param metricLog The reserved prepersisted log entry of the metric.
	 * @param metric The metric to run.
	 * @param cover The cover the metric shall run on.
	 */
	public void runStatisticalMeasure(OcdMetricLog metricLog, StatisticalMeasure metric, Cover cover) {
		CustomGraphId gId = new CustomGraphId(cover.getGraph().getId(), cover.getGraph().getUserName());
		CoverId coverId = new CoverId(cover.getId(), gId);
		OcdMetricLogId logId = new OcdMetricLogId(metricLog.getId(), coverId);
		StatisticalMeasureRunnable runnable = new StatisticalMeasureRunnable(logId, metric, cover, this);
		synchronized (metrics) {
			Future<OcdMetricLog> future = executor.<OcdMetricLog>submit(runnable, metricLog);
			metrics.put(logId, future);
		}
	}
	
	/**
	 * Runs a knowledge-driven measure.
	 * @param metricLog The reserved prepersisted log entry of the metric.
	 * @param metric The metric to run.
	 * @param cover The cover the metric shall run on.
	 * @param groundTruth The ground truth cover to be used by the metric.
	 */
	public void runKnowledgeDrivenMeasure(OcdMetricLog metricLog, KnowledgeDrivenMeasure metric, Cover cover, Cover groundTruth) {
		CustomGraphId gId = new CustomGraphId(cover.getGraph().getId(), cover.getGraph().getUserName());
		CoverId coverId = new CoverId(cover.getId(), gId);
		OcdMetricLogId logId = new OcdMetricLogId(metricLog.getId(), coverId);
		KnowledgeDrivenMeasureRunnable runnable = new KnowledgeDrivenMeasureRunnable(logId, metric, cover, groundTruth, this);
		synchronized (metrics) {
			Future<OcdMetricLog> future = executor.<OcdMetricLog>submit(runnable, metricLog);
			metrics.put(logId, future);
		}
	}

	/**
	 * Merges a calculated metric to the persistence context.
	 * Is called from the runnable itself.
	 * @param log The calculated metric.
	 * May be null if error is true.
	 * @param logId The id reserved for the calculated metric.
	 * @param error States whether an error occurred (true) during execution.
	 */
	public void createMetric(OcdMetricLog log, OcdMetricLogId logId, boolean error) {
    	synchronized (metrics) {
    		if(Thread.interrupted()) {
    			Thread.currentThread().interrupt();
    			return;
    		}
    		if(!error) {
    			EntityManager em = entityHandler.getEntityManager();
    			EntityTransaction tx = em.getTransaction();
		    	try {
					tx.begin();				
					OcdMetricLog persistedLog = em.find(OcdMetricLog.class, logId);
					if(persistedLog == null) {
						/*
						 * Should not happen.
						 */
						requestHandler.log(Level.SEVERE, "Log deleted while metric running.");
						throw new IllegalStateException();
					}
					persistedLog.setValue(log.getValue());
					persistedLog.setStatus(ExecutionStatus.COMPLETED);
					tx.commit();
		    	} catch( RuntimeException e ) {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
					error = true;
				}
		    	em.close();
    		}
    		if(error) {
    			EntityManager em = entityHandler.getEntityManager();
    			EntityTransaction tx = em.getTransaction();
    			try {
					tx.begin();
					OcdMetricLog persistedLog = em.find(OcdMetricLog.class, logId);
					if(persistedLog == null) {
						/*
						 * Should not happen.
						 */
						requestHandler.log(Level.SEVERE, "Log deleted while metric running.");
						throw new IllegalStateException();
					}
					persistedLog.setStatus(ExecutionStatus.ERROR);
					tx.commit();
    			} catch( RuntimeException e ) {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
    			}
    			em.close();
			}	
    		unsynchedInterruptMetric(logId);
		}
	}

	/**
	 * Merges a calculated ground truth cover created by a ground truth benchmark to the persistence context.
	 * Is called from the runnable itself.
	 * @param calculatedCover The calculated ground truth cover holding also the corresponding calculated graph.
	 * May be null if error is true.
	 * @param coverId The id reserved for the ground truth cover.
	 * @param error Indicates whether an error occurred (true) during the calculation.
	 */
	public void createGroundTruthCover(Cover calculatedCover, CoverId coverId, boolean error) {
    	synchronized (benchmarks) {
    		if(Thread.interrupted()) {
    			Thread.currentThread().interrupt();
    			return;
    		}
    		if(!error) {
    			EntityManager em = entityHandler.getEntityManager();
    			EntityTransaction tx = em.getTransaction();
    			try {
    				tx.begin();
    				Cover cover = em.find(Cover.class, coverId);
    				if(cover == null) {
    					/*
    					 * Should not happen.
    					 */
    					requestHandler.log(Level.WARNING, "Cover deleted while benchmark running.");
    					throw new IllegalStateException();
    				}
    				cover.getGraph().setStructureFrom(calculatedCover.getGraph());
    				cover.getGraph().getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
    				tx.commit();
    			} catch( RuntimeException ex ) {
    				if( tx != null && tx.isActive() ) tx.rollback();
    				error = true;
    			}
    			em.close();
    			em = entityHandler.getEntityManager();
    			tx = em.getTransaction();
    			try {
    				tx.begin();
    				Cover cover = em.find(Cover.class, coverId);
    				if(cover == null) {
    					/*
    					 * Should not happen.
    					 */
    					requestHandler.log(Level.WARNING, "Cover deleted while benchmark running.");
    					throw new IllegalStateException();
    				}
    				cover.setMemberships(calculatedCover.getMemberships());
    				cover.getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
    				tx.commit();
    			} catch( RuntimeException ex ) {
    				if( tx != null && tx.isActive() ) tx.rollback();
    				error = true;
    			}
    			em.close();
    		}
			if(error) {
				EntityManager em = entityHandler.getEntityManager();
				EntityTransaction tx = em.getTransaction();
				try {
    				tx.begin();
    				Cover cover = em.find(Cover.class, coverId);
    				if(cover == null) {
    					/*
    					 * Should not happen.
    					 */
    					requestHandler.log(Level.SEVERE, "Cover deleted while benchmark running.");
    					throw new IllegalStateException();
    				}
    				CustomGraph graph = cover.getGraph();
					cover.getCreationMethod().setStatus(ExecutionStatus.ERROR);
					graph.getCreationMethod().setStatus(ExecutionStatus.ERROR);
					tx.commit();
				}  catch( RuntimeException e ) {
    				if( tx != null && tx.isActive() ) {
    					tx.rollback();
    				}
    			}
				em.close();
			}
			CustomGraphId graphId = coverId.getGraphId();
			unsynchedInterruptBenchmark(graphId);
    	}
	}
	
	/**
	 * Merges a calculated cover to the persistence context.
	 * Is called from the runnable itself.
	 * @param calculatedCover The calculated cover.
	 * May be null if error is true.
	 * @param coverId The id reserved for the calculated cover.
	 * @param error States whether an error occurred (true) during execution.
	 */
	public void createCover(Cover calculatedCover, CoverId coverId, boolean error) {
    	synchronized (algorithms) {
    		if(Thread.interrupted()) {
    			Thread.currentThread().interrupt();
    			return;
    		}
    		if(!error) {
    			EntityManager em = entityHandler.getEntityManager();
    			EntityTransaction tx = em.getTransaction();
		    	try {
					tx.begin();
					Cover cover = em.find(Cover.class, coverId);
					if(cover == null) {
						/*
						 * Should not happen.
						 */
						requestHandler.log(Level.SEVERE, "Cover deleted while algorithm running.");
						throw new IllegalStateException();
					}
					cover.setMemberships(calculatedCover.getMemberships());
					OcdMetricLog calculatedExecTime = calculatedCover.getMetrics().get(0);
					OcdMetricLog log = new OcdMetricLog(calculatedExecTime.getType(), calculatedExecTime.getValue(), calculatedExecTime.getParameters(), cover);
					log.setStatus(ExecutionStatus.COMPLETED);
					cover.addMetric(log);
					cover.getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
					tx.commit();
		    	} catch( RuntimeException e ) {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
					error = true;
				}
		    	em.close();
    		}
    		if(error) {
    			EntityManager em = entityHandler.getEntityManager();
    			EntityTransaction tx = em.getTransaction();
    			try {
					tx.begin();
					Cover cover = em.find(Cover.class, coverId);
					if(cover == null) {
						/*
						 * Should not happen.
						 */
						requestHandler.log(Level.SEVERE, "Cover deleted while algorithm running.");
						throw new IllegalStateException();
					}
					cover.getCreationMethod().setStatus(ExecutionStatus.ERROR);
					tx.commit();
    			} catch( RuntimeException e ) {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
    			}
    			em.close();
			}	
	    	unsynchedInterruptAlgorithm(coverId);
		}
	}
	
	/**
	 * Merges a calculated CentralityMap to the persistence context.
	 * Is called from the runnable itself.
	 * @param calculatedMap The calculated cover.
	 * May be null if error is true.
	 * @param mapId The id reserved for the calculated cover.
	 * @param error States whether an error occurred (true) during execution.
	 */
	public void createCentralityMap(CentralityMap calculatedMap, CentralityMapId mapId, boolean error) {
    	synchronized (centralityAlgorithms) {
    		if(Thread.interrupted()) {
    			Thread.currentThread().interrupt();
    			return;
    		}
    		if(!error) {
    			EntityManager em = entityHandler.getEntityManager();
    			EntityTransaction tx = em.getTransaction();
		    	try {
					tx.begin();
					CentralityMap map = em.find(CentralityMap.class, mapId);
					if(map == null) {
						/*
						 * Should not happen.
						 */
						requestHandler.log(Level.SEVERE, "Centrality map deleted while algorithm running.");
						throw new IllegalStateException();
					}
					map.setMap(calculatedMap.getMap());
					map.getCreationMethod().setStatus(ExecutionStatus.COMPLETED);
					map.getCreationMethod().setExecutionTime(calculatedMap.getCreationMethod().getExecutionTime());
					tx.commit();
		    	} catch( RuntimeException e ) {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
					error = true;
				}
		    	em.close();
    		}
    		if(error) {
    			EntityManager em = entityHandler.getEntityManager();
    			EntityTransaction tx = em.getTransaction();
    			try {
					tx.begin();
					CentralityMap map = em.find(CentralityMap.class, mapId);
					if(map == null) {
						/*
						 * Should not happen.
						 */
						requestHandler.log(Level.SEVERE, "Centrality map deleted while algorithm running.");
						throw new IllegalStateException();
					}
					map.getCreationMethod().setStatus(ExecutionStatus.ERROR);
					tx.commit();
    			} catch( RuntimeException e ) {
					if( tx != null && tx.isActive() ) {
						tx.rollback();
					}
    			}
    			em.close();
			}	
	    	unsynchedInterruptAlgorithm(mapId);
		}
	}
	
	/**
	 * Interrupts the algorithm creating a cover.
	 * @param coverId The id of the persisted cover reserved for the algorithm result.
	 */
	public void interruptAlgorithm(CoverId coverId) {
		synchronized (algorithms) {
			unsynchedInterruptAlgorithm(coverId);
		}
	}
	
	/**
	 * Interrupts the benchmark creating a graph
	 * (and in case of a ground truth benchmark also creating the corresponding cover).
	 * @param graphId The id of the persisted graph reserved for the benchmark result.
	 */
	public void interruptBenchmark(CustomGraphId graphId) {
		synchronized (benchmarks) {
			unsynchedInterruptBenchmark(graphId);
		}
	}
	
	/**
	 * Interrupts a metric running on a cover.
	 * @param logId The id of the persisted log reserved for the metric result.
	 */
	public void interruptMetric(OcdMetricLogId logId) {
		synchronized (metrics) {
			unsynchedInterruptMetric(logId);
		}
	}
	
	/**
	 * Interrupts the algorithm creating a cover or if already created
	 * all metrics running it.
	 * Note that ground truth benchmarks will not be interrupted.
	 * @param cover The cover.
	 */
	public void interruptAll(Cover cover) {
		synchronized (algorithms) {
			unsynchedInterruptAlgorithm(new CoverId(cover.getId(), new CustomGraphId(cover.getGraph().getId(), cover.getGraph().getUserName())));
		}
		synchronized (metrics) {
			unsynchedInterruptAllMetrics(cover);
		}
	}
	
	/**
	 * Interrupts the algorithm creating the given CentralityMap.
	 * @param map The CentralityMap 
	 */
	public void interruptAll(CentralityMap map) {
		synchronized (centralityAlgorithms) {
			unsynchedInterruptAlgorithm(new CentralityMapId(map.getId(), new CustomGraphId(map.getGraph().getId(), map.getGraph().getUserName())));
		}
	}
	
	/**
	 * Interrupts an algorithm execution without synchronization.
	 * @param coverId The id of the reserved persisted cover being calculated by the algorithm.
	 */
	private void unsynchedInterruptAlgorithm(CoverId coverId) {
		Future<CoverCreationLog> future = algorithms.get(coverId);
		if(future != null) {
			future.cancel(true);
			algorithms.remove(future);
		}
	}
	
	/**
	 * Interrupts a centrality algorithm execution without synchronization.
	 * @param mapId The id of the reserved persisted centrality map being calculated by the algorithm.
	 */
	private void unsynchedInterruptAlgorithm(CentralityMapId mapId) {
		Future<CentralityCreationLog> future = centralityAlgorithms.get(mapId);
		if(future != null) {
			future.cancel(true);
			centralityAlgorithms.remove(future);
		}
	}
	
	/**
	 * Interrupts a benchmark execution without synchronization.
	 * @param graphId The id of the reserved persisted graph the benchmark is calculating.
	 */
	private void unsynchedInterruptBenchmark(CustomGraphId graphId) {
		Future<GraphCreationLog> future = benchmarks.get(graphId);
		if(future != null) {
			future.cancel(true);
			benchmarks.remove(future);
		}
	}
	
	/**
	 * Interrupts a metric execution without synchronization.
	 * @param cover The cover the metric is run on.
	 * @param logId The id of the reserved persisted log the metric is calculating.
	 */
	private void unsynchedInterruptMetric(OcdMetricLogId logId) {
		Future<OcdMetricLog> future = metrics.get(logId);
		if(future != null) {
			future.cancel(true);
			metrics.remove(future);
		}
	}
	
	/**
	 * Interrupts the execution of all metric running on a cover without synchronization.
	 * @param cover The cover.
	 */
	private void unsynchedInterruptAllMetrics(Cover cover) {
		CoverId coverId = new CoverId(cover.getId(), new CustomGraphId(cover.getGraph().getId(), cover.getGraph().getUserName()));
		for(OcdMetricLog log : cover.getMetrics()) {
			OcdMetricLogId logId = new OcdMetricLogId(log.getId(), coverId);
			unsynchedInterruptMetric(logId);
		}
	}
}
