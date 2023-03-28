package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CoverId;
import i5.las2peer.services.ocd.graphs.CustomGraphId;
import i5.las2peer.services.ocd.metrics.KnowledgeDrivenMeasure;
import i5.las2peer.services.ocd.metrics.OcdMetricExecutor;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricLogId;

import java.util.logging.Level;

/**
 * Runnable for the execution of knowledge-driven measures.
 * @author Sebastian
 *
 */
public class KnowledgeDrivenMeasureRunnable implements Runnable {

	/**
	 * The id of the persisted metric log reserved for the metric result.
	 */
	private OcdMetricLogId logId;
	/**
	 * The metric to execute.
	 */
	private KnowledgeDrivenMeasure metric;
	/**
	 * The cover to run the metric on.
	 */
	private Cover cover;
	/**
	 * The ground truth cover used for the metric execution.
	 * Must be based on the same graph as the cover the metric is run on.
	 */
	private Cover groundTruth;
	/**
	 * The thread handler in charge of the runnable execution.
	 */
	private ThreadHandler threadHandler;
	
	/**
	 * Creates a new instance.
	 * @param logId Sets the log id.
	 * @param metric Sets the metric.
	 * @param cover Sets the cover.
	 * @param groundTruth Sets the ground truth.
	 * @param threadHandler Sets the thread handler.
	 */
	public KnowledgeDrivenMeasureRunnable(OcdMetricLogId logId, KnowledgeDrivenMeasure metric, Cover cover, Cover groundTruth, ThreadHandler threadHandler) {
		this.logId = logId;
		this.metric = metric;
		this.cover = cover;
		this.groundTruth = groundTruth;
		this.threadHandler = threadHandler;
	}
	
	@Override
	public void run() {

		boolean error = false;
		/*
		 * Set metric state to running.
		 */
    	RequestHandler requestHandler = new RequestHandler();
		Database database = new Database(false);

		try {
			OcdMetricLog persistedLog = database.getOcdMetricLog(logId);
			if(persistedLog == null) {
				System.out.println("metric log deleted while metric running");
				/*
				 * Should not happen.
				 */
				requestHandler.log(Level.SEVERE, "Log deleted while metric running.");
				throw new IllegalStateException();
			}
			persistedLog.setStatus(ExecutionStatus.RUNNING);
			database.updateOcdMetricLog(persistedLog);
		} catch( RuntimeException e ) {
			error = true;
		}
		/*
		 * Run metric.
		 */
		OcdMetricLog resultLog = null;
		if(!error) {
	        OcdMetricExecutor executor = new OcdMetricExecutor();
	        try {
	        	resultLog = executor.executeKnowledgeDrivenMeasure(cover, groundTruth, metric);
	        	if(Thread.interrupted()) {
	        		throw new InterruptedException();
	        	}
	        }
	        catch (InterruptedException e) {
	        	return;
	        }
			catch (Exception e) {
				requestHandler.log(Level.SEVERE, "Metric Failure.", e);
				error = true;
			}
		}
    	threadHandler.createMetric(resultLog, logId, error);
	}

}
