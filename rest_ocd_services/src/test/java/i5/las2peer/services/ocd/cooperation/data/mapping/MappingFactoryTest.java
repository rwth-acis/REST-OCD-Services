package i5.las2peer.services.ocd.cooperation.data.mapping;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import i5.las2peer.services.ocd.cooperation.data.mapping.CoverSimulationGroupMapping;
import i5.las2peer.services.ocd.cooperation.data.mapping.CoverSimulationSeriesMapping;
import i5.las2peer.services.ocd.cooperation.data.mapping.MappingFactory;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationDataset;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesGroup;
import i5.las2peer.services.ocd.graphs.Community;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;

@RunWith(MockitoJUnitRunner.class)
public class MappingFactoryTest {
	
	@Spy Cover cover;

	@Spy SimulationSeries simulationSeries;
	
	@Mock
	Community community1;
	@Mock
	Community community2;
	@Mock
	Community community3;
	
	@Mock SimulationSeries simulationSeries1;
	@Mock SimulationSeries simulationSeries2;
	@Mock SimulationSeries simulationSeries3;
	
	@Mock SimulationSeriesGroup simulationSeriesGroup1;
	@Mock SimulationSeriesGroup simulationSeriesGroup2;
	@Mock SimulationSeriesGroup simulationSeriesGroup3;
	
	@Mock
	SimulationDataset simulationDataset1;
	@Mock
	SimulationDataset simulationDataset2;
	@Mock
	SimulationDataset simulationDataset3;

	@Test
	public void buildCoverSimulationSeriesMapping() {
			
		Cover cover = new Cover(new CustomGraph());
		cover.setName("testCover23");
		SimulationSeries simulationSeries = new SimulationSeries();	
		
		MappingFactory factory = new MappingFactory();
		CoverSimulationSeriesMapping mapping = factory.build(cover, simulationSeries);
		assertEquals(cover, mapping.getCover());
		assertEquals("testCover23", mapping.getCover().getName());
		assertEquals(simulationSeries, mapping.getSimulation());		
		
	}
	
	@Test
	public void buildCoverSimulationGroupMapping() {
		
		Cover cover = new Cover(new CustomGraph());
		cover.setName("testCover11");
		
		SimulationSeriesGroup simulation = new SimulationSeriesGroup();
		simulation.setName("testSimulation");
		
		MappingFactory factory = new MappingFactory();
		CoverSimulationGroupMapping mapping = factory.build(cover, simulation);
		assertEquals(cover, mapping.getCover());
		assertEquals("testCover11", mapping.getCover().getName());
		assertEquals(simulation, mapping.getSimulation());		
		
	}

	


}
