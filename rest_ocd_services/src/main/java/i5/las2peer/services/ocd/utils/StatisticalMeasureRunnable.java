package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.metrics.OcdMetricExecutor;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricLogId;
import i5.las2peer.services.ocd.metrics.StatisticalMeasure;

import java.util.logging.Level;

/**
 * Runnable for the execution of statistical measures.
 * @author Sebastian
 *
 */
public class StatisticalMeasureRunnable implements Runnable {

	/**
	 * The id of the persisted metric log reserved for the metric result.
	 */
	private OcdMetricLogId logId;
	/**
	 * The metric to execute.
	 */
	private StatisticalMeasure metric;
	/**
	 * The cover to run the metric on.
	 */
	private Cover cover;
	/**
	 * The thread handler in charge of the runnable execution.
	 */
	private ThreadHandler threadHandler;

	/**
	 * Creates a new instance.
	 * @param logId Sets the log id.
	 * @param metric Sets the metric.
	 * @param cover Sets the cover.
	 * @param threadHandler Sets the thread handler.
	 */
	public StatisticalMeasureRunnable(OcdMetricLogId logId, StatisticalMeasure metric, Cover cover, ThreadHandler threadHandler) {
		this.cover = cover;
		this.metric = metric;
		this.threadHandler = threadHandler;
		this.logId = logId;
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
	        	resultLog = executor.executeStatisticalMeasure(cover, metric);
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
