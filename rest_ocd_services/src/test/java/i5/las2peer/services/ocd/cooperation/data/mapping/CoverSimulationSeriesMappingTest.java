package i5.las2peer.services.ocd.cooperation.data.mapping;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import i5.las2peer.services.ocd.cooperation.data.mapping.CoverSimulationSeriesMapping;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.graphs.Community;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;


@ExtendWith(MockitoExtension.class)
public class CoverSimulationSeriesMappingTest {
	
	@Mock Cover cover;
	@Mock Community community1;
	@Mock Community community2;
	@Mock Community community3;
	@Mock SimulationSeries simulation;
	
	@Test
	public void getPropertyValues() {
		
		CoverSimulationSeriesMapping mapping = new CoverSimulationSeriesMapping();
		mapping.setCover(cover);
		mapping.setSimulation(simulation);
		
		Mockito.doReturn(3).when(cover).communityCount();
		Mockito.doReturn(0.42).when(cover).getCommunityProperty(0, GraphProperty.SIZE);
		Mockito.doReturn(0.11).when(cover).getCommunityProperty(1, GraphProperty.SIZE);
		Mockito.doReturn(0.73).when(cover).getCommunityProperty(2, GraphProperty.SIZE);
		
		double[] result = mapping.getPropertyValues(GraphProperty.SIZE);
		assertNotNull(result);
		assertEquals(3, result.length);
		assertEquals(0.42, result[0], 0.000001);
		assertEquals(0.11, result[1], 0.000001);
		assertEquals(0.73, result[2], 0.000001);

	}

	@Test
	public void getCooperationValues() {
		
		CoverSimulationSeriesMapping mapping = new CoverSimulationSeriesMapping();
		mapping.setCover(cover);
		mapping.setSimulation(simulation);
		
		List<Community> communityList = new ArrayList<>();
		communityList.add(community1);
		Mockito.doReturn(communityList).when(cover).getCommunities();
		Mockito.doReturn(new double[]{0.2,0.3, 0.1}).when(simulation).getAverageCommunityCooperationValues(communityList);
		double[] result = mapping.getCooperationValues();
		assertNotNull(result);
		assertEquals(3, result.length);
		assertEquals(0.2, result[0], 0.000001);
		assertEquals(0.3, result[1], 0.000001);
		assertEquals(0.1, result[2], 0.000001);

		
	}

}
