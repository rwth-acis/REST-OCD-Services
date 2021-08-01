package i5.las2peer.services.ocd.cooperation.data.mapping;

import java.util.List;

import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesGroup;
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
	
	public SimulationGroupSetMapping buildGroupMapping(List<SimulationSeriesGroup> simulation, String name) {
		
		SimulationGroupSetMapping mapping = new SimulationGroupSetMapping();
		mapping.setSimulation(simulation);
		mapping.setName("mapping_" + name);
		
		return mapping;		
	}

	public SimulationSeriesSetMapping build(List<SimulationSeries> simulationSeries, String name) {
		
		SimulationSeriesSetMapping mapping = new SimulationSeriesSetMapping();
		mapping.setSimulation(simulationSeries);
		mapping.setName("mapping_" + name);
		
		return mapping;		
	}

}
