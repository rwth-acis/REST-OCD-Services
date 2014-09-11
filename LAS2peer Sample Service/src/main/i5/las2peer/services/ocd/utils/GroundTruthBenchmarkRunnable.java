package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmarkModel;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmarkExecutor;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverId;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class GroundTruthBenchmarkRunnable implements Runnable {

	/**
	 * The persistence representation of the ground truth cover being calculated.
	 */
	private CoverId coverId;
	/**
	 * The benchmark used for calculating a ground truth cover.
	 */
	private GroundTruthBenchmarkModel benchmark;
	/**
	 * The thread handler in charge of the calculation.
	 */
	private ThreadHandler threadHandler;
	
	public GroundTruthBenchmarkRunnable(CoverId coverId, GroundTruthBenchmarkModel benchmark, ThreadHandler threadHandler) {
		this.coverId = coverId;
		this.benchmark = benchmark;
		this.threadHandler = threadHandler;
	}

	@Override
	public void run() {
		boolean error = false;
		/*
		 * Set algorithm and benchmark status to running.
		 */
		RequestHandler requestHandler = new RequestHandler();
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
			cover.getAlgorithm().setStatus(ExecutionStatus.RUNNING);
			graph.getBenchmark().setStatus(ExecutionStatus.RUNNING);
			tx.commit();
		}  catch( RuntimeException e ) {
			if( tx != null && tx.isActive() ) {
				tx.rollback();
			}
			error = true;
		}
		em.close();
		Cover groundTruthCover = null;
		if(!error) {
			try {
				OcdBenchmarkExecutor executor = new OcdBenchmarkExecutor();
				groundTruthCover = executor.calculateGroundTruthBenchmark(benchmark);
				if(Thread.interrupted()) {
	        		throw new InterruptedException();
	        	}
			}
	        catch (InterruptedException e) {
	        	return;
	        }
			catch (Exception e) {
				requestHandler.log(Level.SEVERE, "Benchmark Failure.", e);
				error = true;
			}
		}
		threadHandler.createGroundTruthCover(groundTruthCover, coverId, error);
	}
}
