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
public class SimulationDatasetTest {
	

	
	@Spy SimulationDataset simulation;
	
	@Mock AgentData data1;
	@Mock AgentData data2;
	@Mock AgentData data3;
	
	@Mock Community community1;
	@Mock Community community2;
	@Mock Community community3;
	
	@Test
	public void setCooperationValues() {
		
		double val1 = 3.235;
		double val2 = 400.534;
		double val3 = 0.235;
				
		List<Double> values = new ArrayList<>();
		values.add(val1);
		values.add(val2);
		values.add(val3);
		
		SimulationDataset simulation = new SimulationDataset();
		simulation.setCooperationValues(values);
		List<Double> result = simulation.getCooperationValues();
		assertEquals(3, result.size());	
		assertEquals(val1, result.get(0), 0.0001);
		assertEquals(val2, result.get(1), 0.0001);	
		assertEquals(val3, result.get(2), 0.0001);
		
	}
	
	@Test
	public void setPayoffValues() {
		
		double val1 = 3.235;
		double val2 = 400.534;
		double val3 = 0.235;
				
		List<Double> values = new ArrayList<>();
		values.add(val1);
		values.add(val2);
		values.add(val3);
		
		SimulationDataset simulation = new SimulationDataset();
		simulation.setPayoffValues(values);
		List<Double> result = simulation.getPayoffValues();
		assertEquals(3, result.size());
		assertEquals(val1, result.get(0), 0.0001);
		assertEquals(val2, result.get(1), 0.0001);	
		assertEquals(val3, result.get(2), 0.0001);
		
	}
		
	@Test
	public void getCommunityCooperationValues() {
		
		List<AgentData> agentList = new ArrayList<>();
		agentList.add(data1);
		agentList.add(data2);
		agentList.add(data3);
		simulation.setAgentData(agentList);
		
		List<Community> communityList = new ArrayList<>();
		communityList.add(community1);
		communityList.add(community2);
		communityList.add(community3);
		
		List<Integer> memberList1 = new ArrayList<>();
		memberList1.add(0);
		
		List<Integer> memberList2 = new ArrayList<>();
		memberList2.add(0);
		memberList2.add(1);
		
		List<Integer> memberList3 = new ArrayList<>();
		memberList3.add(1);
		memberList3.add(2);
		
		Mockito.when(community1.getMemberIndices()).thenReturn(memberList1);
		Mockito.when(community2.getMemberIndices()).thenReturn(memberList2);
		Mockito.when(community3.getMemberIndices()).thenReturn(memberList3);
				
		double[] results;		
		
		Mockito.when(data1.getCooperativity()).thenReturn(1.0);
		Mockito.when(data2.getCooperativity()).thenReturn(1.0);
		Mockito.when(data3.getCooperativity()).thenReturn(1.0);
		
		results = simulation.getCommunityCooperationValues(communityList);
		assertEquals(3, results.length);
		assertEquals(1.0, results[0], 0.001);
		assertEquals(1.0, results[1], 0.001);
		assertEquals(1.0, results[2], 0.001);
		
		Mockito.when(data1.getCooperativity()).thenReturn(0.0);
		Mockito.when(data2.getCooperativity()).thenReturn(0.0);
		Mockito.when(data3.getCooperativity()).thenReturn(0.0);
		
		results = simulation.getCommunityCooperationValues(communityList);
		assertEquals(3, results.length);
		assertEquals(0.0, results[0], 0.001);
		assertEquals(0.0, results[1], 0.001);
		assertEquals(0.0, results[2], 0.001);
		
		Mockito.when(data1.getCooperativity()).thenReturn(1.0);
		Mockito.when(data2.getCooperativity()).thenReturn(0.0);
		Mockito.when(data3.getCooperativity()).thenReturn(0.0);
		
		results = simulation.getCommunityCooperationValues(communityList);
		assertEquals(3, results.length);
		assertEquals(1.0, results[0], 0.001);
		assertEquals(0.5, results[1], 0.001);
		assertEquals(0.0, results[2], 0.001);
		
		Mockito.when(data1.getCooperativity()).thenReturn(0.92);
		Mockito.when(data2.getCooperativity()).thenReturn(0.46);
		Mockito.when(data3.getCooperativity()).thenReturn(0.14);
		
		results = simulation.getCommunityCooperationValues(communityList);
		assertEquals(3, results.length);
		assertEquals(0.92, results[0], 0.001);
		assertEquals(0.69, results[1], 0.001);
		assertEquals(0.30, results[2], 0.001);
	}

	@Test
	public void getCommunityCooperationValuesInvalidTooLarge() {
		List<AgentData> agentList = new ArrayList<>();
		agentList.add(data1);
		agentList.add(data2);
		agentList.add(data3);
		simulation.setAgentData(agentList);

		List<Community> communityList = new ArrayList<>();
		communityList.add(community1);
		communityList.add(community2);

		List<Integer> memberList1 = new ArrayList<>();
		memberList1.add(4);

		List<Integer> memberList2 = new ArrayList<>();
		memberList2.add(10);
		memberList2.add(3);

		//Mockito.when(community1.getMemberIndices()).thenReturn(memberList1);
		Mockito.when(community2.getMemberIndices()).thenReturn(memberList2);

		assertThrows(IllegalArgumentException.class, () -> {
			simulation.getCommunityCooperationValues(communityList);
		});

	}
	
	@Test
	public void getCommunityCooperationValuesInvalidTooLow() {
		List<AgentData> agentList = new ArrayList<>();
		agentList.add(data1);
		agentList.add(data2);
		agentList.add(data3);
		simulation.setAgentData(agentList);
		
		List<Community> communityList = new ArrayList<>();
		communityList.add(community1);
		communityList.add(community2);
	
		List<Integer> memberList1 = new ArrayList<>();
		memberList1.add(0);
		
		List<Integer> memberList2 = new ArrayList<>();
		memberList2.add(-1);
		memberList2.add(3);		

		Mockito.when(community1.getMemberIndices()).thenReturn(memberList1);
		Mockito.when(community2.getMemberIndices()).thenReturn(memberList2);

		assertThrows(IllegalArgumentException.class, () -> {
			simulation.getCommunityCooperationValues(communityList);
		});
	}
	
	
	@Test
	public void getAgentStrategy() {
		
		SimulationDataset simulation = new SimulationDataset();		
		List<AgentData> agentList = new ArrayList<>();
		agentList.add(data1);
		agentList.add(data2);
		
		Mockito.when(data1.getFinalStrategy()).thenReturn(true);
		Mockito.when(data2.getFinalStrategy()).thenReturn(false);
		simulation.setAgentData(agentList);
		
		
		boolean result = simulation.getAgentStrategy(0);
		assertEquals(true, result);
		
		result = simulation.getAgentStrategy(1);
		assertEquals(false, result);
		
	}
	
	@Test
	public void agentCountZero() {
		
		SimulationDataset simulation = new SimulationDataset();
		int result = simulation.agentCount();
		assertEquals(0, result);
	}
	
	@Test
	public void agentCount() {
		
		SimulationDataset simulation = new SimulationDataset();		
		List<AgentData> agentList = new ArrayList<>();
		agentList.add(data1);
		agentList.add(data2);
		agentList.add(data3);		
		
		simulation.setAgentData(agentList);		
		int result = simulation.agentCount();
		assertEquals(3, result);
		
	}
	
}
