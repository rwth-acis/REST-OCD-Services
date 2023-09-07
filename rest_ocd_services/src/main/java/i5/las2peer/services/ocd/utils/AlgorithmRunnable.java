package i5.las2peer.services.ocd.utils;

import java.util.logging.Level;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithmExecutor;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverId;
import i5.las2peer.services.ocd.graphs.CustomGraphId;

/**
 * Runnable for the execution of ocd algorithms.
 * @author Sebastian
 *
 */
public class AlgorithmRunnable implements Runnable {

	/**
	 * The persisted cover reserved for the algorithm result.
	 */
	private Cover cover;
	/**
	 * The algorithm to execute.
	 */
	private OcdAlgorithm algorithm;
	/**
	 * The component node count filter used by the OcdAlgorithmExecutor.
	 */
	private int componentNodeCountFilter;
	/**
	 * The thread handler in charge of the runnable execution.
	 */
	private ThreadHandler threadHandler;
	
	/**
	 * Creates a new instance.
	 * @param cover Sets the cover.
	 * @param algorithm Sets the algorithm.
	 * @param componentNodeCountFilter Sets the component node count filter.
	 * @param threadHandler Sets the thread handler.
	 */
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
		String cKey = cover.getKey();
		String gKey = cover.getGraph().getKey();
		String user = cover.getGraph().getUserName();
		CustomGraphId graphId = new CustomGraphId(gKey, user);
		CoverId coverId = new CoverId(cKey, graphId);

    	RequestHandler requestHandler = new RequestHandler();

		Database database = new Database(false);
		try {
			Cover c = database.getCover(user, gKey,  cKey);
			if(c == null) {
				//System.out.println("Cover in AR run was null " + user + gKey + cKey);
				/*
				 * Should not happen.
				 */
				requestHandler.log(Level.SEVERE, "Cover deleted while algorithm running.");
				throw new IllegalStateException();
			}
			c.getCreationMethod().setStatus(ExecutionStatus.RUNNING);
			database.updateCoverCreationLog(c);
		} catch( Exception e ) {
			error = true;
		}

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
    	threadHandler.createCover(resultCover, coverId, error);

	}

}
