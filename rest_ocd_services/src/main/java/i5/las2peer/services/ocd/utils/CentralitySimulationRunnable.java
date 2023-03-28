package i5.las2peer.services.ocd.utils;

import java.util.logging.Level;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.data.CentralityMapId;
import i5.las2peer.services.ocd.centrality.utils.CentralitySimulation;
import i5.las2peer.services.ocd.centrality.utils.CentralitySimulationExecutor;
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
		String mKey = map.getKey();
		String gKey = map.getGraph().getKey();
		String user = map.getGraph().getUserName();
		CustomGraphId graphId = new CustomGraphId(gKey, user);		
    	CentralityMapId id = new CentralityMapId(mKey, graphId);
    	
    	RequestHandler requestHandler = new RequestHandler();

    	Database database = new Database(false);
		try {
			CentralityMap m = database.getCentralityMap(user, gKey, mKey);
			if(m == null) {
				/*
				 * Should not happen.
				 */
				System.out.println("Centrality map deleted while simulation running.");
				requestHandler.log(Level.SEVERE, "Centrality map deleted while simulation running.");
				throw new IllegalStateException();
			}
			m.getCreationMethod().setStatus(ExecutionStatus.RUNNING);
			database.updateCentralityCreationLog(m);
		} catch( RuntimeException e ) {

			e.printStackTrace();
			error = true;
		}

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
