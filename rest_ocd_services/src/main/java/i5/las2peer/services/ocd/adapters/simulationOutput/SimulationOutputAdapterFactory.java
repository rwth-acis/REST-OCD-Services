package i5.las2peer.services.ocd.adapters.simulationOutput;

import i5.las2peer.services.ocd.utils.SimpleFactory;

/**
 * A factory for producing simulation output adapters using simulation output format objects as descriptors.
 *
 */
public class SimulationOutputAdapterFactory implements SimpleFactory<SimulationOutputAdapter, SimulationOutputFormat> {

	@Override
	public SimulationOutputAdapter getInstance(SimulationOutputFormat outputFormat)
			throws InstantiationException, IllegalAccessException {

		return outputFormat.getAdapterClass().newInstance();
	}

}
