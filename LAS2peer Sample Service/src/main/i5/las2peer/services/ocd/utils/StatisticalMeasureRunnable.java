package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.metrics.OcdMetricExecutor;
import i5.las2peer.services.ocd.metrics.OcdMetricLog;
import i5.las2peer.services.ocd.metrics.OcdMetricLogId;
import i5.las2peer.services.ocd.metrics.StatisticalMeasure;

import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class StatisticalMeasureRunnable implements Runnable {

	private Cover cover;
	private StatisticalMeasure metric;
	private ThreadHandler threadHandler;
	private OcdMetricLogId logId;
	
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
    	EntityManager em = requestHandler.getEntityManager();
    	EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			OcdMetricLog persistedLog = em.find(OcdMetricLog.class, logId);
			if(persistedLog == null) {
				/*
				 * Should not happen.
				 */
				requestHandler.log(Level.SEVERE, "Log deleted while metric running.");
				throw new IllegalStateException();
			}
			persistedLog.setStatus(ExecutionStatus.RUNNING);
			tx.commit();
		} catch( RuntimeException e ) {
			if( tx != null && tx.isActive() ) {
				tx.rollback();
			}
			error = true;
		}
		em.close();
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
