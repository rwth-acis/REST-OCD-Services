package i5.las2peer.services.ocd.cooperation.data.simulation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import i5.las2peer.services.ocd.graphs.Community;


@ExtendWith(MockitoExtension.class)
public class SimulationSeriesGroupTest {

	@Spy SimulationSeriesGroup simulation;
	
	List<SimulationSeries> seriesList = new ArrayList<>();
	List<Community> communityList = new ArrayList<>();
	
	@Mock
	SimulationSeries series1;
	@Mock
	SimulationSeries series2;
	@Mock
	SimulationSeries series3;

	@Mock
	SimulationSeriesParameters para1;
	@Mock
	SimulationSeriesParameters para2;

	@Mock
	Evaluation eval1;
	@Mock
	Evaluation eval2;

	@Mock
	Community community1;
	@Mock
	Community community2;
	@Mock
	Community community3;
	
	public void initSeries() {

		seriesList = new ArrayList<>();
		seriesList.add(series1);
		seriesList.add(series2);
		seriesList.add(series3);
	}
		
	@Test
	public void getAverageCommunityCooperationValue() {

		SimulationSeriesGroup simulation = new SimulationSeriesGroup();

		List<Integer> memberList = new ArrayList<>();
		Mockito.when(community1.getMemberIndices()).thenReturn(memberList);

		initSeries();
		simulation.setSimulationSeries(seriesList);

		Mockito.when(series1.getAverageCommunityCooperationValue(community1)).thenReturn(0.4);
		Mockito.when(series2.getAverageCommunityCooperationValue(community1)).thenReturn(0.795);
		Mockito.when(series3.getAverageCommunityCooperationValue(community1)).thenReturn(0.005);

		double result = simulation.getAverageCommunityCooperationValue(community1);
		assertEquals(0.4, result, 0.00000001);

		Mockito.when(series1.getAverageCommunityCooperationValue(community1)).thenReturn(0.98765435);
		Mockito.when(series2.getAverageCommunityCooperationValue(community1))
				.thenReturn(0.26543462232);
		Mockito.when(series3.getAverageCommunityCooperationValue(community1)).thenReturn(0.000004);

		result = simulation.getAverageCommunityCooperationValue(community1);
		assertEquals(0.41769765744, result, 0.00000001);

	}

	@Test
	public void getAverageCommunityCooperationValueInvalidCommunity() {


		SimulationSeriesGroup simulation = new SimulationSeriesGroup();
		seriesList.add(series1);
		simulation.setSimulationSeries(seriesList);
		Mockito.when(community1.getMemberIndices()).thenReturn(null);


		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			simulation.getAverageCommunityCooperationValue(community1);
		});

		assertNotNull(thrown);

	}

	@Test
	public void getAverageCommunityCooperationValueEmpty() {
		SimulationSeriesGroup simulation = new SimulationSeriesGroup();

		IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
			simulation.getAverageCommunityCooperationValue(community1);
		});

		assertNotNull(thrown);

	}
	
	@Test
	public void getFinalCooperationValues() {

		SimulationSeriesGroup simulation = new SimulationSeriesGroup();
		initSeries();
		simulation.setSimulationSeries(seriesList);

		Mockito.when(series1.averageCooperationValue()).thenReturn(0.13);
		Mockito.when(series2.averageCooperationValue()).thenReturn(0.27);
		Mockito.when(series3.averageCooperationValue()).thenReturn(0.74);

		double[] result = simulation.getAverageFinalCooperationValues();
		assertNotNull(result);
		assertEquals(3, result.length);
		assertEquals(0.13, result[0], 0.00000001);
		assertEquals(0.27, result[1], 0.00000001);
		assertEquals(0.74, result[2], 0.00000001);
			
	}

	@Test
	public void getFinalPayoffValues() {
		
		SimulationSeriesGroup simulation = new SimulationSeriesGroup();
		initSeries();
		simulation.setSimulationSeries(seriesList);

		Mockito.when(series1.averagePayoffValue()).thenReturn(0.13);
		Mockito.when(series2.averagePayoffValue()).thenReturn(0.27);
		Mockito.when(series3.averagePayoffValue()).thenReturn(0.74);

		double[] result = simulation.getAverageFinalPayoffValues();
		assertNotNull(result);
		assertEquals(3, result.length);
		assertEquals(0.13, result[0], 0.00000001);
		assertEquals(0.27, result[1], 0.00000001);
		assertEquals(0.74, result[2], 0.00000001);
		
	}

}
