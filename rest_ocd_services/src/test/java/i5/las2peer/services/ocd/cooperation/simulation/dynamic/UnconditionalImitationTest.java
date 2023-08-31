package i5.las2peer.services.ocd.cooperation.simulation.dynamic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.AdditionalMatchers;
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
public class UnconditionalImitationTest {
	
	@Mock
	private MersenneTwisterFast random;
	
	@Mock Simulation simulation;
	
	@Mock
	Agent agent1;
	@Mock
	Agent agent2;
	@Mock
	Agent agent3;
	@Mock
	Agent agent4;

	@Spy
	UnconditionalImitation imitation;
		
	@Test
	public void getNewStrategyAlgorithm() {

		UnconditionalImitation dynamic = new UnconditionalImitation();

		boolean strategy;
		int size = 3;
		boolean[] strategies;
		double[] payoff;

		strategies = new boolean[] { false, false, true };
		payoff = new double[] { 3.0, 1.0, 2.0 };
		strategy = dynamic.getNewStrategy(size, strategies, payoff);
		assertEquals(false, strategy);

		strategies = new boolean[] { false, false, true };
		payoff = new double[] { 2.0, 1.0, 3.0 };
		strategy = dynamic.getNewStrategy(size, strategies, payoff);
		assertEquals(true, strategy);
		
	}
	
	@Test
	public void testGetNewStrategyDependencies() {		
				
		int round = 4;
		
		boolean[] strategy = new boolean[4];		
		strategy[0] = true;
		strategy[1] = false;
		strategy[2] = true;
		strategy[3] = false;
		
		double[] payoff = new double[4];
		payoff[0] = 0.0;
		payoff[1] = 1.0;
		payoff[2] = 2.0;
		payoff[3] = 3.0;
		
		Bag neighbourBag = new Bag();
		neighbourBag.add(agent1);
		neighbourBag.add(agent2);
		neighbourBag.add(agent3);
		
		Mockito.when(simulation.getRound()).thenReturn(round);
		Mockito.when(agent4.getNeighbourhood()).thenReturn(neighbourBag);
		
		Mockito.when(agent1.getPayoff(round - 1)).thenReturn(payoff[0]);
		Mockito.when(agent2.getPayoff(round - 1)).thenReturn(payoff[1]);
		Mockito.when(agent3.getPayoff(round - 1)).thenReturn(payoff[2]);
		Mockito.when(agent4.getPayoff(round - 1)).thenReturn(payoff[3]);
		Mockito.when(agent1.getStrategy(round - 1)).thenReturn(strategy[0]);
		Mockito.when(agent2.getStrategy(round - 1)).thenReturn(strategy[1]);
		Mockito.when(agent3.getStrategy(round - 1)).thenReturn(strategy[2]);
		Mockito.when(agent4.getStrategy(round - 1)).thenReturn(strategy[3]);						
		
		boolean resultStrategy = imitation.getNewStrategy(agent4, simulation);

		Mockito.verify(imitation, Mockito.times(1)).getNewStrategy(Matchers.eq(4), AdditionalMatchers.aryEq(strategy),
				AdditionalMatchers.aryEq(payoff));

	}
	
	@Test
	public void getDynamicType() {
		
		UnconditionalImitation dynamic = new UnconditionalImitation();
		DynamicType type = dynamic.getDynamicType();
		assertEquals(type, DynamicType.UNCONDITIONAL_IMITATION);
	}

}
