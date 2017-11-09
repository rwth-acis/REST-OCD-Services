package i5.las2peer.services.ocd.utils;

import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.data.CentralityMapId;
import i5.las2peer.services.ocd.centrality.simulations.CentralitySimulation;
import i5.las2peer.services.ocd.centrality.simulations.CentralitySimulationExecutor;
import i5.las2peer.services.ocd.graphs.CustomGraphId;

public class CentralitySimulationRunnable implements Runnable {
	
	/**
	 * The persisted CentralityMap reserved for the algorithm result.
	 */
	private CentralityMap map;
	/**
	 * The algorithm to execute.
	 */
	private CentralitySimulation simulation;
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
	 * @param simulation Sets the CentralityAlgorithm.
	 * @param threadHandler Sets the thread handler.
	 */
	public CentralitySimulationRunnable(CentralityMap map, CentralitySimulation simulation, ThreadHandler threadHandler) {
		this.simulation = simulation;
		this.map = map;
		this.threadHandler = threadHandler;
	}
	
	@Override
	public void run() {
		boolean error = false;
		/*
		 * Set simulation state to running.
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
				requestHandler.log(Level.SEVERE, "Centrality map deleted while simulation running.");
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
		 * Run simulation.
		 */
		CentralityMap resultMap = null;
		if(!error) {
	        CentralitySimulationExecutor executor = new CentralitySimulationExecutor();
	        try {
	        	resultMap = executor.execute(map.getGraph(), simulation);
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
