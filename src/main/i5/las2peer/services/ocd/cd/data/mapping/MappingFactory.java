package i5.las2peer.services.ocd.cd.data.mapping;

import i5.las2peer.services.ocd.cd.data.simulation.SimulationList;
import i5.las2peer.services.ocd.cd.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.cd.data.simulation.SimulationSeriesGroup;
import i5.las2peer.services.ocd.graphs.Cover;

public class MappingFactory {
		
	public CoverSimulationSeriesMapping build(Cover cover, SimulationSeries simulation) {
		
		CoverSimulationSeriesMapping mapping = new CoverSimulationSeriesMapping();
		mapping.setCover(cover);
		mapping.setSimulation(simulation);
		mapping.setName("mapping_" + simulation.getName() + "_" + cover.getName());
		
		return mapping;		
	}
	
	public CoverSimulationGroupMapping build(Cover cover, SimulationSeriesGroup simulation) {
		
		CoverSimulationGroupMapping mapping = new CoverSimulationGroupMapping();
		mapping.setCover(cover);
		mapping.setSimulation(simulation);
		mapping.setName("mapping_" + simulation.getName() + "_" + cover.getName());
		
		return mapping;		
	}
	
	public SimulationGroupSetMapping build(SimulationList simulation, String name) {
		
		SimulationGroupSetMapping mapping = new SimulationGroupSetMapping();
		mapping.setSimulation(simulation);
		mapping.setName("mapping_" + name);
		
		return mapping;		
	}
	


}
