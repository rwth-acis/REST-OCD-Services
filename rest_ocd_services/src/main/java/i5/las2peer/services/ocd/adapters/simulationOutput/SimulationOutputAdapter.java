package i5.las2peer.services.ocd.adapters.simulationOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.OutputAdapter;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationAbstract;

public interface SimulationOutputAdapter extends OutputAdapter {
	
	void write(SimulationAbstract simulation) throws AdapterException;
	
}
