package i5.las2peer.services.ocd.utils;

import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.data.CentralityMapId;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmExecutor;
import i5.las2peer.services.ocd.graphs.CustomGraphId;

/**
 * Runnable for the execution of centrality algorithms.
 * @author Tobias
 *
 */
public class CentralityAlgorithmRunnable implements Runnable {

	/**
	 * The persisted CentralityMap reserved for the algorithm result.
	 */
	private CentralityMap map;
	/**
	 * The algorithm to execute.
	 */
	private CentralityAlgorithm algorithm;
	/**
	 * The thread handler in charge of the runnable execution.
	 */
	private ThreadHandler threadHandler;
	/**
	 * The entity handler in charge of accessing persisted data.
	 */
	private EntityHandler entityHandler = new EntityHandler();
	
	/**
	 * Creates a new instance.
	 * @param map Sets the CentralityMap.
	 * @param algorithm Sets the CentralityAlgorithm.
	 * @param threadHandler Sets the thread handler.
	 */
	public CentralityAlgorithmRunnable(CentralityMap map, CentralityAlgorithm algorithm, ThreadHandler threadHandler) {
		this.algorithm = algorithm;
		this.map = map;
		this.threadHandler = threadHandler;
	}
	
	@Override
	public void run() {
		boolean error = false;
		/*
		 * Set algorithm state to running.
		 */
		CustomGraphId graphId = new CustomGraphId(map.getGraph().getId(), map.getGraph().getUserName());
    	CentralityMapId id = new CentralityMapId(map.getId(), graphId);
    	RequestHandler requestHandler = new RequestHandler();
    	EntityManager em = entityHandler.getEntityManager();
    	EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			CentralityMap map = em.find(CentralityMap.class, id);
			if(map == null) {
				/*
				 * Should not happen.
				 */
				requestHandler.log(Level.SEVERE, "Centrality map deleted while algorithm running.");
				throw new IllegalStateException();
			}
			map.getCreationMethod().setStatus(ExecutionStatus.RUNNING);
			tx.commit();
		} catch( RuntimeException e ) {
			if( tx != null && tx.isActive() ) {
				tx.rollback();
			}
			error = true;
		}
		em.close();
		/*
		 * Run algorithm.
		 */
		CentralityMap resultMap = null;
		if(!error) {
	        CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
	        try {
	        	resultMap = executor.execute(map.getGraph(), algorithm);
	        	if(Thread.interrupted()) {
	        		throw new InterruptedException();
	        	}
	        }
	        catch (InterruptedException e) {
	        	return;
	        }
			catch (Exception e) {
				requestHandler.log(Level.SEVERE, "Algorithm Failure.", e);
				error = true;
			}
		}
    	threadHandler.createCentralityMap(resultMap, id, error);
	}

}
