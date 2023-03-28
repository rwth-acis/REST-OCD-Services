package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmark;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmarkExecutor;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverId;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomGraphId;

import java.util.logging.Level;

/**
 * Runnable for the execution of ground truth benchmarks.
 * @author Sebastian
 *
 */
public class GroundTruthBenchmarkRunnable implements Runnable {

	/**
	 * The id of the persisted cover reserved for the benchmark result.
	 */
	private CoverId coverId;
	/**
	 * The benchmark to execute.
	 */
	private GroundTruthBenchmark benchmark;
	/**
	 * The thread handler in charge of the runnable execution.
	 */
	private ThreadHandler threadHandler;
	
	/**
	 * Creates a new instance.
	 * @param coverId Sets the cover id.
	 * @param benchmark Sets the benchmark.
	 * @param threadHandler Sets the thread handler.
	 */
	public GroundTruthBenchmarkRunnable(CoverId coverId, GroundTruthBenchmark benchmark, ThreadHandler threadHandler) {
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

		Database database = new Database(false);
		
		String cKey = coverId.getKey();
		CustomGraphId gId = coverId.getGraphId();
		String user = gId.getUser();
		String gKey = gId.getKey();
		try {
			Cover cover = database.getCover(user, gKey, cKey);
			if(cover == null) {
				System.out.println("Cover in GroundTruthBenchmarkRunnable runnable was null " + user + gKey + cKey);
				/*
				 * Should not happen.
				 */
				requestHandler.log(Level.SEVERE, "Cover deleted while benchmark running.");
				throw new IllegalStateException();
			}
			CustomGraph graph = cover.getGraph();
			cover.getCreationMethod().setStatus(ExecutionStatus.RUNNING);
			graph.getCreationMethod().setStatus(ExecutionStatus.RUNNING);
			database.updateGraphCreationLog(graph);	//TODO both in one transaction?
			database.updateCoverCreationLog(cover);
		}  catch( RuntimeException e ) {
			error = true;
		}
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
