package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.utils.ConditionalParameterizableFactory;

import java.util.Map;

/**
 * A factory for producing ocd metrics using ocd metric type objects as descriptors.
 * @author Sebastian
 *
 */
public class OcdMetricFactory implements ConditionalParameterizableFactory<OcdMetric, OcdMetricType> {

	@Override
	public OcdMetric getInstance(OcdMetricType metricType, Map<String, String> parameters) throws InstantiationException, IllegalAccessException {
		if(isInstantiatable(metricType)) {
			OcdMetric metric = metricType.getMetricClass().newInstance();
			metric.setParameters(parameters);
			return metric;
		}
		throw new IllegalStateException("This metric is not instantiatable.");
	}
	
	@Override
	public boolean isInstantiatable(OcdMetricType metricType) {
		if(metricType.getMetricClass().equals(OcdMetric.class)) {
			return false;
		}
		else {
			return true;
		}
	}

}
