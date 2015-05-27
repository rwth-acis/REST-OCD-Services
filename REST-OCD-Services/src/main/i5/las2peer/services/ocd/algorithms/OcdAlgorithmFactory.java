package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.graphs.CoverCreationType;
import i5.las2peer.services.ocd.utils.ConditionalParameterizableFactory;

import java.util.Map;

/**
 * A factory for producing ocd algorithms using cover creation type objects as descriptors.
 * @author Sebastian
 *
 */
public class OcdAlgorithmFactory implements ConditionalParameterizableFactory<OcdAlgorithm, CoverCreationType> {

	@Override
	public OcdAlgorithm getInstance(CoverCreationType creationType, Map<String, String> parameters) throws InstantiationException, IllegalAccessException {
		if(isInstantiatable(creationType)) {
			OcdAlgorithm algorithm = (OcdAlgorithm) creationType.getCreationMethodClass().newInstance();
			algorithm.setParameters(parameters);
			return algorithm;
		}
		throw new IllegalStateException("This creation type is not an instantiatable algorithm.");
	}
	
	@Override
	public boolean isInstantiatable(CoverCreationType creationType) {
		if(creationType.correspondsAlgorithm()) {
			return true;
		}
		else {
			return false;
		}
	}

}
