package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.algorithms.AlgorithmLog;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.benchmarks.BenchmarkLog;
import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmarkModel;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverId;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphId;
import i5.las2peer.services.ocd.metrics.MetricLog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Handles the synchronization of threads for running algorithms and metrics.
 * @author Sebastian
 *
 */
public class ThreadHandler {

	private static final int THREAD_POOL_SIZE = 4;
	
	/*
	 * Mapping from the id of a cover in calculation to the future of the algorithm calculating it.
	 */
	private static Map<CoverId, Future<AlgorithmLog>> algorithms = new HashMap<CoverId, Future<AlgorithmLog>>();
	
	/*
	 * Mapping from the id of a cover being measured to the futures of its currently running metrics.
	 */
	private static ListMultimap<CoverId, Future<MetricLog>> metrics = ArrayListMultimap.<CoverId, Future<MetricLog>>create();
	
	/*
	 * Mapping from the id of a graph in calculation to the future of the benchmark calculating it.
	 */
	private static Map<CustomGraphId, Future<BenchmarkLog>> benchmarks = new HashMap<CustomGraphId, Future<BenchmarkLog>>();
	
	/*
	 * A thread pool for the execution of metric and algorithm runnables.
	 */
	private static ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	
	private RequestHandler requestHandler = new RequestHandler();
	
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
		AlgorithmLog log = cover.getAlgorithm();
		synchronized (algorithms) {
			Future<AlgorithmLog> future = executor.<AlgorithmLog>submit(runnable, log);
			algorithms.put(coverId, future);
		}
	}
	
	/**
	 * Runs a ground truth benchmark.
	 * @param coverId The id reserved for the ground truth cover.
	 * @param benchmark The benchmark model to calculate the ground truth cover with.
	 */
	public void runGroundTruthBenchmark(Cover cover, GroundTruthBenchmarkModel benchmark) {
		CustomGraphId gId = new CustomGraphId(cover.getGraph().getId(), cover.getGraph().getUserName());
		CoverId coverId = new CoverId(cover.getId(), gId);
		GroundTruthBenchmarkRunnable runnable = new GroundTruthBenchmarkRunnable(coverId, benchmark, this);
		BenchmarkLog log = cover.getGraph().getBenchmark();
		synchronized (benchmarks) {
			Future<BenchmarkLog> future = executor.<BenchmarkLog>submit(runnable, log);
			benchmarks.put(gId, future);
		}
	}

	/**
	 * Merges a calculated ground truth cover created by a ground truth benchmark to the persistence context.
	 * Is called from the runnable itself.
	 * @param calculatedCover The ground truth cover with a valid id and holding the benchmark graph with valid id and username.
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
    			EntityManager em = requestHandler.getEntityManager();
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
    				cover.getGraph().getBenchmark().setStatus(ExecutionStatus.COMPLETED);
    				tx.commit();
    			} catch( RuntimeException ex ) {
    				if( tx != null && tx.isActive() ) tx.rollback();
    				error = true;
    			}
    			em.close();
    			em = requestHandler.getEntityManager();
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
    				cover.getAlgorithm().setStatus(ExecutionStatus.COMPLETED);
    				tx.commit();
    			} catch( RuntimeException ex ) {
    				if( tx != null && tx.isActive() ) tx.rollback();
    				error = true;
    			}
    			em.close();
    		}
			if(error) {
				EntityManager em = requestHandler.getEntityManager();
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
					cover.getAlgorithm().setStatus(ExecutionStatus.ERROR);
					graph.getBenchmark().setStatus(ExecutionStatus.ERROR);
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
    			EntityManager em = requestHandler.getEntityManager();
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
					MetricLog calculatedExecTime = calculatedCover.getMetrics().get(0);
					MetricLog log = new MetricLog(calculatedExecTime.getType(), calculatedExecTime.getValue(), calculatedExecTime.getParameters(), cover);
					log.setStatus(ExecutionStatus.COMPLETED);
					cover.addMetric(log);
					cover.getAlgorithm().setStatus(ExecutionStatus.COMPLETED);
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
    			EntityManager em = requestHandler.getEntityManager();
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
					cover.getAlgorithm().setStatus(ExecutionStatus.ERROR);
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
	 * Interrupts the algorithm creating a cover.
	 * @param cover The cover.
	 */
	public void interruptAlgorithm(CoverId coverId) {
		synchronized (algorithms) {
			unsynchedInterruptAlgorithm(coverId);
		}
	}
	
	/**
	 * Interrupts the benchmark creating a graph (and possibly a corresponding cover).
	 * @param cover The cover.
	 */
	public void interruptBenchmark(CustomGraphId graphId) {
		synchronized (benchmarks) {
			unsynchedInterruptBenchmark(graphId);
		}
	}
	
	/**
	 * Interrupts a metric running on a cover.
	 * @param cover The cover.
	 * @param log The log corresponding the metric.
	 */
	public void interruptMetric(CoverId coverId, MetricLog log) {
		synchronized (metrics) {
			unsynchedInterruptMetric(coverId, log);
		}
	}
	
	/**
	 * Interrupts the algorithm creating a cover or if already created
	 * all metrics running it.
	 * Note that ground truth benchmarks will not be interrupted.
	 * @param cover The cover.
	 */
	public void interruptAll(CoverId coverId) {
		synchronized (algorithms) {
			unsynchedInterruptAlgorithm(coverId);
		}
		synchronized (metrics) {
			unsynchedInterruptAllMetrics(coverId);
		}
	}
	
	/*
	 * Unsynchronized interruption of an algorithm calculating a cover.
	 * @param cover The cover.
	 */
	private void unsynchedInterruptAlgorithm(CoverId coverId) {
		Future<AlgorithmLog> future = algorithms.get(coverId);
		if(future != null) {
			future.cancel(true);
			algorithms.remove(future);
		}
	}
	
	private void unsynchedInterruptBenchmark(CustomGraphId graphId) {
		Future<BenchmarkLog> future = benchmarks.get(graphId);
		if(future != null) {
			future.cancel(true);
			benchmarks.remove(future);
		}
	}
	
	/*
	 * Unsynchronized interruption of a running metric of a cover. 
	 * @param cover The cover.
	 * @param log The log corresponding the metric.
	 */
	private void unsynchedInterruptMetric(CoverId coverId, MetricLog log) {
		for(Future<MetricLog> future : metrics.get(coverId)) {
			try {
				if(future.get().getId() == log.getId()) {
					future.cancel(true);
					metrics.remove(coverId, future);
					return;
				}
			} catch (InterruptedException | ExecutionException e) {
				/*
				 * Future in invalid state.
				 * Should not happen.
				 */
				future.cancel(true);
				metrics.remove(coverId, future);
				requestHandler.log(Level.SEVERE, "Unexpected metric breakdown. Metric future was in an invalid state.");
			}
		}
		throw new IllegalStateException();
	}
	
	/*
	 * Unsynchronized interruption of all running metrics of a cover.
	 * @param cover The cover.
	 */
	private void unsynchedInterruptAllMetrics(CoverId coverId) {
		for(Future<MetricLog> future : metrics.get(coverId)) {
			future.cancel(true);
			metrics.remove(coverId, future);
		}
	}
}
