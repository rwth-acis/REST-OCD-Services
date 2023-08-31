package i5.las2peer.services.ocd.cooperation.data.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import org.mockito.junit.jupiter.MockitoExtension;

import i5.las2peer.services.ocd.graphs.Community;


@ExtendWith(MockitoExtension.class)
public class SimulationSeriesTest {

	@Spy
	SimulationSeries simulation;

	List<SimulationDataset> datasetList = new ArrayList<>();
	List<Community> communityList = new ArrayList<>();

	@Mock
	SimulationDataset dataset1;
	@Mock
	SimulationDataset dataset2;
	@Mock
	SimulationDataset dataset3;

	@Mock
	Community community1;
	@Mock
	Community community2;
	@Mock
	Community community3;

	public void initDatasets() {

		datasetList = new ArrayList<>();
		datasetList.add(dataset1);
		datasetList.add(dataset2);
		datasetList.add(dataset3);
	}

	@Test
	public void generations() {

		SimulationSeries simulation = new SimulationSeries();

		initDatasets();
		Mockito.when(dataset1.generations()).thenReturn(14);
		Mockito.when(dataset2.generations()).thenReturn(27);
		Mockito.when(dataset3.generations()).thenReturn(0);

		simulation.setSimulationDatasets(datasetList);

		int result = simulation.generations();
		assertEquals(27, result);
	}

	@Test
	public void generationsNull() {

		SimulationSeries simulation = new SimulationSeries();
		int result = simulation.generations();
		assertEquals(0, result);
	}

	@Test
	public void getAverageCommunityCooperationValue() {

		SimulationSeries simulation = new SimulationSeries();

		List<Integer> memberList = new ArrayList<>();
		Mockito.when(community1.getMemberIndices()).thenReturn(memberList);

		initDatasets();
		simulation.setSimulationDatasets(datasetList);

		Mockito.when(dataset1.getCommunityCooperationValue(Matchers.anyListOf(Integer.class))).thenReturn(0.4);
		Mockito.when(dataset2.getCommunityCooperationValue(Matchers.anyListOf(Integer.class))).thenReturn(0.795);
		Mockito.when(dataset3.getCommunityCooperationValue(Matchers.anyListOf(Integer.class))).thenReturn(0.005);

		double result = simulation.getAverageCommunityCooperationValue(community1);
		assertEquals(0.4, result, 0.00000001);

		Mockito.when(dataset1.getCommunityCooperationValue(Matchers.anyListOf(Integer.class))).thenReturn(0.98765435);
		Mockito.when(dataset2.getCommunityCooperationValue(Matchers.anyListOf(Integer.class)))
				.thenReturn(0.26543462232);
		Mockito.when(dataset3.getCommunityCooperationValue(Matchers.anyListOf(Integer.class))).thenReturn(0.000004);

		result = simulation.getAverageCommunityCooperationValue(community1);
		assertEquals(0.41769765744, result, 0.00000001);

	}

	@Test
	public void getAverageCommunityCooperationValueInvalidCommunity() {

		SimulationSeries simulation = new SimulationSeries();
		datasetList.add(dataset1);
		simulation.setSimulationDatasets(datasetList);
		Mockito.when(community1.getMemberIndices()).thenReturn(null);


		assertThrows(IllegalArgumentException.class, () ->simulation.getAverageCommunityCooperationValue(community1));


	}

	@Test
	public void getAverageCommunityCooperationValueEmpty() {

		SimulationSeries simulation = new SimulationSeries();
		//Mockito.when(community1.getMemberIndices()).thenReturn(null); // unnecessary stubbing
		assertThrows(IllegalStateException.class, () ->simulation.getAverageCommunityCooperationValue(community1));


	}

	@Test
	public void getAverageCommunityCooperationValues() {

		Mockito.doCallRealMethod().when(simulation)
				.getAverageCommunityCooperationValues(Matchers.anyListOf(Community.class));
		Mockito.doReturn(0.4).when(simulation).getAverageCommunityCooperationValue(community1);
		Mockito.doReturn(0.6).when(simulation).getAverageCommunityCooperationValue(community2);
		Mockito.doReturn(0.2).when(simulation).getAverageCommunityCooperationValue(community3);
		Mockito.doReturn(5).when(simulation).size();

		communityList = new ArrayList<>();
		communityList.add(community1);
		communityList.add(community2);
		communityList.add(community3);

		double[] result = simulation.getAverageCommunityCooperationValues(communityList);
		assertNotNull(result);
		assertEquals(3, result.length);
		assertEquals(0.4, result[0], 0.00000001);
		assertEquals(0.6, result[1], 0.00000001);
		assertEquals(0.2, result[2], 0.00000001);

		Mockito.verify(simulation, Mockito.times(1)).getAverageCommunityCooperationValue(community1);
		Mockito.verify(simulation, Mockito.times(1)).getAverageCommunityCooperationValue(community2);
		Mockito.verify(simulation, Mockito.times(1)).getAverageCommunityCooperationValue(community3);

	}

	@Test
	public void getFinalCooperationValues() {

		initDatasets();
		simulation.setSimulationDatasets(datasetList);

		Mockito.when(dataset1.getFinalCooperationValue()).thenReturn(0.13);
		Mockito.when(dataset2.getFinalCooperationValue()).thenReturn(0.27);
		Mockito.when(dataset3.getFinalCooperationValue()).thenReturn(0.31);

		double[] result = simulation.getFinalCooperationValues();
		assertNotNull(result);
		assertEquals(3, result.length);
		assertEquals(0.13, result[0], 0.00000001);
		assertEquals(0.27, result[1], 0.00000001);
		assertEquals(0.31, result[2], 0.00000001);
			
	}

	@Test
	public void getFinalPayoffValues() {
		
		initDatasets();
		simulation.setSimulationDatasets(datasetList);

		Mockito.when(dataset1.getFinalPayoffValue()).thenReturn(0.13);
		Mockito.when(dataset2.getFinalPayoffValue()).thenReturn(0.27);
		Mockito.when(dataset3.getFinalPayoffValue()).thenReturn(0.74);

		double[] result = simulation.getFinalPayoffValues();
		assertNotNull(result);
		assertEquals(3, result.length);
		assertEquals(0.13, result[0], 0.00000001);
		assertEquals(0.27, result[1], 0.00000001);
		assertEquals(0.74, result[2], 0.00000001);
		
	}

	@Test
	public void getCooperationValuesOverTime() {
		
		initDatasets();
		simulation.setSimulationDatasets(datasetList);
		
		List<Double> values1 = new ArrayList<>();
		values1.add(1.1);
		values1.add(2.5);
		values1.add(0.01);
		
		List<Double> values2 = new ArrayList<>();
		values2.add(1.9);
		values2.add(1.5);
		values2.add(0.0);
		
		List<Double> values3 = new ArrayList<>();
		values3.add(6.0);
		values3.add(2.0);
		values3.add(0.3);
		
		Mockito.doReturn(3).when(simulation).generations();
		Mockito.doReturn(datasetList).when(simulation).getSimulationDatasets();
		Mockito.doReturn(values1).when(dataset1).getCooperationValues();
		Mockito.doReturn(values2).when(dataset2).getCooperationValues();
		Mockito.doReturn(values3).when(dataset3).getCooperationValues();
		
		double[] result = simulation.getAverageCooperationValuesOverTime();
		assertNotNull(result);
		assertEquals(3, result.length);
		assertEquals(3.0, result[0], 0.00000001);
		assertEquals(2.0, result[1], 0.00000001);
		assertEquals(0.10333333, result[2], 0.00000001);
		
	}

	@Test
	public void evaluate() {
		
		double[] cooperation = new double[]{0.1, 0.2, 0.3, 0.4};
		double[] payoff = new double[]{0.1, 0.2, 0.3, 0.4};
		
		Mockito.doReturn(cooperation).when(simulation).getFinalCooperationValues();
		Mockito.doReturn(payoff).when(simulation).getFinalPayoffValues();
		
		simulation.evaluate();
		Evaluation evaluation = simulation.getCooperationEvaluation();
		assertNotNull(evaluation);
		
		evaluation = simulation.getPayoffEvaluation();
		assertNotNull(evaluation);
		
		Mockito.verify(simulation, Mockito.times(1)).getFinalCooperationValues();
		Mockito.verify(simulation, Mockito.times(1)).getFinalPayoffValues();
	}
	
	@Test
	public void size() {
		
		SimulationSeries simulation = new SimulationSeries();
		int result;
		
		datasetList = new ArrayList<>();
		simulation.setSimulationDatasets(datasetList);
		result = simulation.size();
		assertEquals(0, result);
		
		datasetList.add(dataset1);
		simulation.setSimulationDatasets(datasetList);
		result = simulation.size();
		assertEquals(1, result);
		
		datasetList.add(dataset2);
		datasetList.add(dataset3);
		simulation.setSimulationDatasets(datasetList);
		result = simulation.size();
		assertEquals(3, result);
		
	}
	
	@Test
	public void averageCooperationValue() {
		
		simulation.setCooperationEvaluation(null);		
		double[] cooperation = new double[]{0.04, 0.26, 0.85, 0.45};
		Mockito.doReturn(cooperation).when(simulation).getFinalCooperationValues();
		
		double result = simulation.averageCooperationValue();

		assertEquals(0.4, result, 0.00000001);
	}
	
	@Test
	public void averagePayoffValue() {
		
		simulation.setPayoffEvaluation(null);		
		double[] values = new double[]{0.31, 0.01, 0.65, 0.95};
		Mockito.doReturn(values).when(simulation).getFinalPayoffValues();
		
		double result = simulation.averagePayoffValue();

		assertEquals(0.48, result, 0.00000001);
		
	}
		
	
}
