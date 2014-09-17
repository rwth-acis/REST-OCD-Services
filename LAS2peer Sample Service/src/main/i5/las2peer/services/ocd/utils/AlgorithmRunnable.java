package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithmExecutor;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverId;
import i5.las2peer.services.ocd.graphs.CustomGraphId;

import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class AlgorithmRunnable implements Runnable {

	private Cover cover;
	private OcdAlgorithm algorithm;
	private int componentNodeCountFilter;
	private ThreadHandler threadHandler;
	
	public AlgorithmRunnable(Cover cover, OcdAlgorithm algorithm, int componentNodeCountFilter, ThreadHandler threadHandler) {
		this.algorithm = algorithm;
		this.cover = cover;
		this.componentNodeCountFilter = componentNodeCountFilter;
		this.threadHandler = threadHandler;
	}
	
	@Override
	public void run() {
		boolean error = false;
		/*
		 * Set algorithm state to running.
		 */
		CustomGraphId graphId = new CustomGraphId(cover.getGraph().getId(), cover.getGraph().getUserName());
    	CoverId id = new CoverId(cover.getId(), graphId);
    	RequestHandler requestHandler = new RequestHandler();
    	EntityManager em = requestHandler.getEntityManager();
    	EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			Cover cover = em.find(Cover.class, id);
			if(cover == null) {
				/*
				 * Should not happen.
				 */
				requestHandler.log(Level.SEVERE, "Cover deleted while algorithm running.");
				throw new IllegalStateException();
			}
			cover.getCreationMethod().setStatus(ExecutionStatus.RUNNING);
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
		Cover resultCover = null;
		if(!error) {
	        OcdAlgorithmExecutor executor = new OcdAlgorithmExecutor();
	        try {
	        	resultCover = executor.execute(cover.getGraph(), algorithm, componentNodeCountFilter);
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
    	threadHandler.createCover(resultCover, id, error);
	}

}
