package i5.las2peer.services.ocd.centrality.simulations;

import java.util.Map;

import i5.las2peer.services.ocd.utils.ConditionalParameterizableFactory;

public class CentralitySimulationFactory implements ConditionalParameterizableFactory<CentralitySimulation, CentralitySimulationType> {

	@Override
	public boolean isInstantiatable(CentralitySimulationType simulationType) {
		if(simulationType.correspondsAlgorithm()) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public CentralitySimulation getInstance(CentralitySimulationType simulationType, Map<String, String> parameters)
			throws InstantiationException, IllegalAccessException {
		if(isInstantiatable(simulationType)) {
			CentralitySimulation simulation = simulationType.getSimulationClass().newInstance();
			simulation.setParameters(parameters);
			return simulation;
		}
		throw new IllegalStateException("This simulation type is not an instantiatable simulation.");
	}

}
