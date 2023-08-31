package i5.las2peer.services.ocd.cooperation.data.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesGroup;
import i5.las2peer.services.ocd.graphs.Community;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.properties.GraphProperty;


@ExtendWith(MockitoExtension.class)
public class CoverSimulationGroupMappingTest {
	
	@Mock Cover cover;
	@Mock Community community1;
	@Mock Community community2;
	@Mock Community community3;
	@Mock SimulationSeriesGroup simulation;
	
	
	@Test
	public void getPropertyValues() {
		
		CoverSimulationGroupMapping mapping = new CoverSimulationGroupMapping();
		mapping.setCover(cover);
		mapping.setSimulation(simulation);
		
		Mockito.doReturn(3).when(cover).communityCount();
		Mockito.doReturn(0.342).when(cover).getCommunityProperty(0, GraphProperty.SIZE);
		Mockito.doReturn(0.112).when(cover).getCommunityProperty(1, GraphProperty.SIZE);
		Mockito.doReturn(0.433).when(cover).getCommunityProperty(2, GraphProperty.SIZE);
		
		double[] result = mapping.getPropertyValues(GraphProperty.SIZE);
		assertNotNull(result);
		assertEquals(3, result.length);
		assertEquals(0.342, result[0], 0.000001);
		assertEquals(0.112, result[1], 0.000001);
		assertEquals(0.433, result[2], 0.000001);
		
		Mockito.doReturn(5).when(cover).communityCount();
		Mockito.doReturn(3.211).when(cover).getCommunityProperty(0, GraphProperty.AVERAGE_DEGREE);
		Mockito.doReturn(4.62).when(cover).getCommunityProperty(1, GraphProperty.AVERAGE_DEGREE);
		Mockito.doReturn(0.234).when(cover).getCommunityProperty(2, GraphProperty.AVERAGE_DEGREE);
		Mockito.doReturn(0.0).when(cover).getCommunityProperty(3, GraphProperty.AVERAGE_DEGREE);
		Mockito.doReturn(12.1).when(cover).getCommunityProperty(4, GraphProperty.AVERAGE_DEGREE);
		
		result = mapping.getPropertyValues(GraphProperty.AVERAGE_DEGREE);
		assertNotNull(result);
		assertEquals(5, result.length);
		assertEquals(3.211, result[0], 0.000001);
		assertEquals(4.62, result[1], 0.000001);
		assertEquals(0.234, result[2], 0.000001);
		assertEquals(0.0, result[3], 0.000001);
		assertEquals(12.1, result[4], 0.000001);

	}

	@Test
	public void getCooperationValues() {
		
		CoverSimulationGroupMapping mapping = new CoverSimulationGroupMapping();
		mapping.setCover(cover);
		mapping.setSimulation(simulation);
		
		List<Community> communityList = new ArrayList<>();
		communityList.add(community1);
		Mockito.doReturn(communityList).when(cover).getCommunities();
		Mockito.doReturn(new double[]{0.2,0.3}).when(simulation).getAverageCommunityCooperationValues(communityList);
		double[] result = mapping.getCooperationValues();
		assertNotNull(result);
		assertEquals(2, result.length);
		assertEquals(0.2, result[0], 0.000001);
		assertEquals(0.3, result[1], 0.000001);
		
		Mockito.doReturn(new double[]{0.333, 0.0, 0.245, 1.0}).when(simulation).getAverageCommunityCooperationValues(communityList);
		result = mapping.getCooperationValues();
		assertNotNull(result);
		assertEquals(4, result.length);
		assertEquals(0.333, result[0], 0.000001);
		assertEquals(0.0, result[1], 0.000001);
		assertEquals(0.245, result[2], 0.000001);
		assertEquals(1.0, result[3], 0.000001);

		
	}
	
}
