package i5.las2peer.services.ocd.centrality.utils;

import java.util.Map;

import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.utils.ConditionalParameterizableFactory;

public class CentralityAlgorithmFactory implements ConditionalParameterizableFactory<CentralityAlgorithm, CentralityMeasureType> {

	public boolean isInstantiatable(CentralityMeasureType creationType) {
		if(creationType.correspondsAlgorithm()) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public CentralityAlgorithm getInstance(CentralityMeasureType centralityMeasureType, Map<String, String> parameters) throws InstantiationException, IllegalAccessException {
		if(isInstantiatable(centralityMeasureType)) {
			CentralityAlgorithm algorithm = (CentralityAlgorithm) centralityMeasureType.getCreationMethodClass().newInstance();
			algorithm.setParameters(parameters);
			return algorithm;
		}
		throw new IllegalStateException("This creation type is not an instantiatable algorithm.");
	}

}
