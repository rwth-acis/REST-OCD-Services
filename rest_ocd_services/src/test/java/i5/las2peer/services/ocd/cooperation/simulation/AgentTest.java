package i5.las2peer.services.ocd.cooperation.simulation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import ec.util.MersenneTwisterFast;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;


@ExtendWith(MockitoExtension.class)
public class AgentTest {

	@Mock
	private MersenneTwisterFast random;

	@Spy
	Agent agent;
	@Mock
	Agent agent1;
	@Mock
	Agent agent2;
	@Mock
	Agent agent3;

	@Mock
	Network network;
	
	@Test
	public void initializeNeighbourhood() {
		
		Bag edges = new Bag(3);
		edges.add(new Edge(agent, agent1, 1));
		edges.add(new Edge(agent, agent2, 1));
		edges.add(new Edge(agent, agent3, 1));
		Mockito.when(network.getEdgesIn(agent)).thenReturn(edges);
		
		Mockito.reset(agent);
		agent.initialize(true, network);
		assertNotNull(agent.getNeighbourhood());
		Mockito.verify(agent, Mockito.times(1)).calculateNeighbourhood(network);
		assertEquals(3, agent.getNeighbourhood().size());

		Mockito.reset(agent);
		agent.initialize(true, network);
		assertNotNull(agent.getNeighbourhood());
		Mockito.verify(agent, Mockito.times(0)).calculateNeighbourhood(network);
		assertEquals(3, agent.getNeighbourhood().size());

	}
	
	@Test
	public void initialize() {
		
		Agent agent = new Agent();
		agent.initialize(true, network);
		assertNotNull(agent.getNeighbourhood());
		assertEquals(true, agent.getStrategy());
		assertEquals(true, agent.getStrategy(0));
		assertEquals(0.0, agent.getPayoff(), 0.01);
		
		agent.initialize(false, network);
		assertEquals(false, agent.getStrategy());
		assertEquals(false, agent.getStrategy(0));
		assertEquals(0.0, agent.getPayoff(), 0.01);
		
	}
		
	@Test
	public void isSteadyTrue() {
		
		Mockito.doReturn(true).when(agent).getStrategy(0);
		Mockito.doReturn(true).when(agent).getStrategy(-1);
		assertTrue(agent.isSteady());
		
		Mockito.doReturn(false).when(agent).getStrategy(0);
		Mockito.doReturn(false).when(agent).getStrategy(-1);
		assertTrue(agent.isSteady());
	}
	
	@Test
	public void isSteadyFalse() {
		
		Mockito.doReturn(false).when(agent).getStrategy(0);
		Mockito.doReturn(true).when(agent).getStrategy(-1);
		assertFalse(agent.isSteady());
		
		Mockito.doReturn(true).when(agent).getStrategy(0);
		Mockito.doReturn(false).when(agent).getStrategy(-1);
		assertFalse(agent.isSteady());
	}

	@Test
	public void getRandomNeighbourTest() {

		Agent result;
		Bag agents = new Bag(3);

		Mockito.when(agent.getNeighbourhood()).thenReturn(agents);
		Mockito.when(agent.getRandomNeighbour(Matchers.any(MersenneTwisterFast.class))).thenCallRealMethod();

		// no neighbor

		result = agent.getRandomNeighbour(random);
		assertEquals(null, result);

		// random neighbor

		agents.add(agent1);
		agents.add(agent2);
		agents.add(agent3);

		Mockito.when(random.nextInt(Matchers.anyInt())).thenReturn(0);
		result = agent.getRandomNeighbour(random);
		assertEquals(agent1, result);

		Mockito.when(random.nextInt(Matchers.anyInt())).thenReturn(1);
		result = agent.getRandomNeighbour(random);
		assertEquals(agent2, result);

		Mockito.when(random.nextInt(Matchers.anyInt())).thenReturn(2);
		result = agent.getRandomNeighbour(random);
		assertEquals(agent3, result);

	}

	@Test
	public void getNeighbourhoodTest() {

		// Calculate new Bag

		Bag resultBag;
		Agent agent = new Agent();
		Network network = new Network(false);
		network.addNode(agent);
		network.addNode(agent1);
		network.addNode(agent2);
		network.addNode(agent3);

		resultBag = agent.calculateNeighbourhood(network);
		assertNotNull(resultBag);
		assertEquals(0, resultBag.size());
		network.addEdge(agent, agent1, true);
		resultBag = agent.calculateNeighbourhood(network);
		assertNotNull(resultBag);
		assertEquals(1, resultBag.size());
		assertEquals(agent1, resultBag.get(0));

		network.addEdge(agent2, agent, true);
		network.addEdge(agent, agent3, true);
		resultBag = agent.calculateNeighbourhood(network);
		assertNotNull(resultBag);
		assertEquals(3, resultBag.size());
		assertEquals(agent1, resultBag.get(0));
		assertEquals(agent2, resultBag.get(1));
		assertEquals(agent3, resultBag.get(2));

	}

}
