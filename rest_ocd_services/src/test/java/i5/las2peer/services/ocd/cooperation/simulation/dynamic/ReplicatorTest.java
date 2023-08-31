package i5.las2peer.services.ocd.cooperation.simulation.dynamic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import ec.util.MersenneTwisterFast;
import i5.las2peer.services.ocd.cooperation.simulation.Agent;
import i5.las2peer.services.ocd.cooperation.simulation.Simulation;
import sim.util.Bag;


@ExtendWith(MockitoExtension.class)
public class ReplicatorTest {

	@Mock
	private MersenneTwisterFast random;

	@Mock
	Agent agent1;
	@Mock
	Agent agent2;

	@Spy
	Replicator replicator;
	
	@Test
	public void testGetDynamicType() {
		
		Replicator replicator = new Replicator(1);
		DynamicType type = replicator.getDynamicType();
		assertEquals(type, DynamicType.REPLICATOR);
	}
	
	@Test
	public void testGetNewStrategyGreaterMyPayoff() {

		Replicator replicator = new Replicator(1);

		boolean strategy;
		boolean myStrategy;
		boolean otherStrategy;
		int myNeighSize = 1;
		int otherNeighSize = 2;
		int value = 1;
		double myPayoff = 2.0;
		double otherPayoff = 1.0;

		myStrategy = true;
		otherStrategy = false;
		strategy = replicator.getNewStrategy(myStrategy, otherStrategy, myPayoff, otherPayoff, random, myNeighSize,
				otherNeighSize, value);
		assertEquals(true, strategy);

		myStrategy = false;
		otherStrategy = true;
		strategy = replicator.getNewStrategy(myStrategy, otherStrategy, myPayoff, otherPayoff, random, myNeighSize,
				otherNeighSize, value);
		assertEquals(false, strategy);

		myStrategy = false;
		otherStrategy = false;
		strategy = replicator.getNewStrategy(myStrategy, otherStrategy, myPayoff, otherPayoff, random, myNeighSize,
				otherNeighSize, value);
		assertEquals(false, strategy);

		myStrategy = true;
		otherStrategy = true;
		strategy = replicator.getNewStrategy(myStrategy, otherStrategy, myPayoff, otherPayoff, random, myNeighSize,
				otherNeighSize, value);
		assertEquals(true, strategy);
	}

	@Test
	public void testGetNewStrategyGreaterOtherPayoffLowRandom() {

		Replicator replicator = new Replicator(1);

		boolean strategy;
		boolean myStrategy = true;
		boolean otherStrategy = false;
		int myNeighSize = 1;
		int otherNeighSize = 2;
		int value = 1;
		double myPayoff = 1.0;
		double otherPayoff = 2.0;

		Mockito.when(random.nextDouble(true, true)).thenReturn(0.2);

		myStrategy = true;
		otherStrategy = false;
		strategy = replicator.getNewStrategy(myStrategy, otherStrategy, myPayoff, otherPayoff, random, myNeighSize,
				otherNeighSize, value);
		assertEquals(otherStrategy, strategy);

		myStrategy = false;
		otherStrategy = true;
		strategy = replicator.getNewStrategy(myStrategy, otherStrategy, myPayoff, otherPayoff, random, myNeighSize,
				otherNeighSize, value);
		assertEquals(otherStrategy, strategy);

		myStrategy = false;
		otherStrategy = false;
		strategy = replicator.getNewStrategy(myStrategy, otherStrategy, myPayoff, otherPayoff, random, myNeighSize,
				otherNeighSize, value);
		assertEquals(otherStrategy, strategy);

		myStrategy = true;
		otherStrategy = true;
		strategy = replicator.getNewStrategy(myStrategy, otherStrategy, myPayoff, otherPayoff, random, myNeighSize,
				otherNeighSize, value);
		assertEquals(otherStrategy, strategy);

	}

	@Test
	public void testGetNewStrategyGreaterOtherPayoffHighRandom() {
		
		Replicator replicator = new Replicator(1);

		boolean strategy;
		boolean myStrategy = true;
		boolean otherStrategy = false;
		int myNeighSize = 1;
		int otherNeighSize = 2;
		int value = 1;
		double myPayoff = 1.0;
		double otherPayoff = 2.0;
		
		Mockito.when(random.nextDouble(true, true)).thenReturn(0.8);

		myStrategy = true;
		otherStrategy = false;
		strategy = replicator.getNewStrategy(myStrategy, otherStrategy, myPayoff, otherPayoff, random, myNeighSize,
				otherNeighSize, value);
		assertEquals(myStrategy, strategy);

		myStrategy = false;
		otherStrategy = true;
		strategy = replicator.getNewStrategy(myStrategy, otherStrategy, myPayoff, otherPayoff, random, myNeighSize,
				otherNeighSize, value);
		assertEquals(myStrategy, strategy);

		myStrategy = false;
		otherStrategy = false;
		strategy = replicator.getNewStrategy(myStrategy, otherStrategy, myPayoff, otherPayoff, random, myNeighSize,
				otherNeighSize, value);
		assertEquals(myStrategy, strategy);

		myStrategy = true;
		otherStrategy = true;
		strategy = replicator.getNewStrategy(myStrategy, otherStrategy, myPayoff, otherPayoff, random, myNeighSize,
				otherNeighSize, value);
		assertEquals(myStrategy, strategy);

	}

	@Test
	public void testGetNewStrategyDependencies() {

		Simulation simulation = new Simulation();
		boolean myStrategy = false;
		boolean otherStrategy = true;
		double myPayoff = 4.0;
		double otherPayoff = 3.0;
		int myNeighSize = 2;
		int otherNeighSize = 2;
		double value = 1.5;

		Mockito.when(agent1.getPayoff(Matchers.anyInt())).thenReturn(myPayoff);
		Mockito.when(agent2.getPayoff(Matchers.anyInt())).thenReturn(otherPayoff);
		Mockito.when(agent1.getStrategy(Matchers.anyInt())).thenReturn(myStrategy);
		Mockito.when(agent2.getStrategy(Matchers.anyInt())).thenReturn(otherStrategy);
		Mockito.when(agent1.getRandomNeighbour(Matchers.any())).thenReturn(agent2);
		//Mockito.when(agent2.getRandomNeighbour(Matchers.any())).thenReturn(agent1); //unnecessary stabbing

		Bag bag = new Bag();
		bag.add(1);
		bag.add(2);
		Mockito.when(agent1.getNeighbourhood()).thenReturn(bag);
		Mockito.when(agent2.getNeighbourhood()).thenReturn(bag);
		Mockito.when(replicator.getValues()).thenReturn(new double[] { value });

		boolean strategy = replicator.getNewStrategy(agent1, simulation);

		Mockito.verify(replicator, Mockito.times(1)).getNewStrategy(Matchers.eq(myStrategy), Matchers.eq(otherStrategy),
				Matchers.eq(myPayoff), Matchers.eq(otherPayoff), Matchers.isA(MersenneTwisterFast.class),
				Matchers.eq(myNeighSize), Matchers.eq(otherNeighSize), Matchers.eq(value));

	}
}
