package i5.las2peer.services.ocd.benchmarks;

import i5.las2peer.services.ocd.graphs.GraphCreationType;
import i5.las2peer.services.ocd.utils.ConditionalParameterizableFactory;

import java.util.Map;

/**
 * A factory for producing ocd benchmarks using graph creation type objects as descriptors.
 * @author Sebastian
 *
 */
public class OcdBenchmarkFactory implements ConditionalParameterizableFactory<OcdBenchmark, GraphCreationType> {

	@Override
	public OcdBenchmark getInstance(GraphCreationType creationType, Map<String, String> parameters) throws InstantiationException, IllegalAccessException {
		if(isInstantiatable(creationType)) {
			OcdBenchmark benchmark = (OcdBenchmark) creationType.getCreationMethodClass().newInstance();
			benchmark.setParameters(parameters);
			return benchmark;
		}
		throw new IllegalStateException("This creation type is not an instantiatable benchmark.");
	}
	
	@Override
	public boolean isInstantiatable(GraphCreationType creationType) {
		if(creationType.correspondsBenchmark()) {
			return true;
		}
		else {
			return false;
		}
	}

}
