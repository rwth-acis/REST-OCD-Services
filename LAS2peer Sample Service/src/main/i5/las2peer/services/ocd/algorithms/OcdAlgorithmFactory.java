package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.utils.ConditionalParameterizableFactory;

import java.util.Map;

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
		if(creationType.isAlgorithm()) {
			return true;
		}
		else {
			return false;
		}
	}

}
