package i5.las2peer.services.ocd.cooperation.simulation;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Matchers.*;
import org.mockito.runners.MockitoJUnitRunner;

import i5.las2peer.services.ocd.cooperation.simulation.Agent;
import i5.las2peer.services.ocd.cooperation.simulation.Simulation;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.Dynamic;
import i5.las2peer.services.ocd.cooperation.simulation.game.Game;
import sim.field.network.Network;
import sim.util.Bag;

@RunWith(MockitoJUnitRunner.class)
public class SimulationTest {

	@Mock
	Network network;

	@Mock
	Game game;

	@Mock
	Dynamic dynamic;

	@Mock
	Agent agent0;
	@Mock
	Agent agent1;
	@Mock
	Agent agent2;
	@Mock
	Agent agent3;

	@Mock
	Bag agentsBag;

	@Before
	public void setUpAgents() {

		Mockito.when(network.getAllNodes()).thenReturn(agentsBag);
		Mockito.when(agentsBag.size()).thenReturn(4);
		Mockito.when(agentsBag.get(0)).thenReturn(agent0);
		Mockito.when(agentsBag.get(1)).thenReturn(agent1);
		Mockito.when(agentsBag.get(2)).thenReturn(agent2);
		Mockito.when(agentsBag.get(3)).thenReturn(agent3);

	}

	@Test
	public void getCooperationTest() {

		int coopNumber;
		double coopValue;
		Simulation simulation = new Simulation(1, network, game, dynamic);

		Mockito.when(agent0.getStrategy()).thenReturn(true);
		Mockito.when(agent1.getStrategy()).thenReturn(true);
		Mockito.when(agent2.getStrategy()).thenReturn(true);
		Mockito.when(agent3.getStrategy()).thenReturn(true);
		coopNumber = simulation.getCooperationNumber();
		assertEquals(4, coopNumber);
		coopValue = simulation.getCooperationValue();
		assertEquals(1.0, 1.0, coopValue);

		Mockito.when(agent0.getStrategy()).thenReturn(false);
		Mockito.when(agent1.getStrategy()).thenReturn(true);
		Mockito.when(agent2.getStrategy()).thenReturn(true);
		Mockito.when(agent3.getStrategy()).thenReturn(false);
		coopNumber = simulation.getCooperationNumber();
		assertEquals(2, coopNumber);
		coopValue = simulation.getCooperationValue();
		assertEquals(0.5, 0.5, coopValue);

		Mockito.when(agent0.getStrategy()).thenReturn(false);
		Mockito.when(agent1.getStrategy()).thenReturn(false);
		Mockito.when(agent2.getStrategy()).thenReturn(false);
		Mockito.when(agent3.getStrategy()).thenReturn(false);
		coopNumber = simulation.getCooperationNumber();
		assertEquals(0, coopNumber);
		coopValue = simulation.getCooperationValue();
		assertEquals(0.0, 0.0, coopValue);

	}

	@Test
	public void getPayoffTest() {

		double total;
		double average;
		Simulation simulation = new Simulation(1, network, game, dynamic);

		Mockito.when(agent0.getPayoff(anyInt())).thenReturn(2.0);
		Mockito.when(agent1.getPayoff(anyInt())).thenReturn(4.0);
		Mockito.when(agent2.getPayoff(anyInt())).thenReturn(3.2);
		Mockito.when(agent3.getPayoff(anyInt())).thenReturn(1.5);
		total = simulation.getCooperationNumber();
		assertEquals(10.7, 10.7, total);
		average = simulation.getAveragePayoff();
		assertEquals(2.675, 2.675, average);

		Mockito.when(agent0.getPayoff(anyInt())).thenReturn(-1.2);
		Mockito.when(agent1.getPayoff(anyInt())).thenReturn(3.4);
		Mockito.when(agent2.getPayoff(anyInt())).thenReturn(0.0);
		Mockito.when(agent3.getPayoff(anyInt())).thenReturn(1.6);
		total = simulation.getCooperationNumber();
		assertEquals(3.8, 3.8, total);
		average = simulation.getAveragePayoff();
		assertEquals(0.95, 0.95, average);

	}
	
	
	

}
