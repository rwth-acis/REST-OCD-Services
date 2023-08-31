package i5.las2peer.services.ocd.cooperation.simulation.game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;

import i5.las2peer.services.ocd.cooperation.simulation.Agent;
import sim.util.Bag;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GameTest {

	double trueTrue = 1.0;
	double trueFalse = 2.0;
	double falseTrue = 3.0;
	double falseFalse = 4.0;

	@Spy
	Game game;

	@Mock
	Agent agent;
	@Mock
	Agent neigh1;
	@Mock
	Agent neigh2;
	@Mock
	Agent neigh3;

	@Test
	public void getPayoffByStrategyAA() {

		game = new Game(trueTrue, trueFalse, falseTrue, falseFalse);
		boolean myStrategy = true;
		boolean otherStrategy = true;
		double payoff = game.getPayoff(myStrategy, otherStrategy);
		assertEquals(trueTrue, payoff, 0.01);

	}

	@Test
	public void getPayoffByStrategyAB() {

		game = new Game(trueTrue, trueFalse, falseTrue, falseFalse);
		boolean myStrategy = true;
		boolean otherStrategy = false;
		double payoff = game.getPayoff(myStrategy, otherStrategy);
		assertEquals(trueFalse, payoff, 0.01);

	}

	@Test
	public void getPayoffByStrategyBA() {

		game = new Game(trueTrue, trueFalse, falseTrue, falseFalse);
		boolean myStrategy = false;
		boolean otherStrategy = true;
		double payoff = game.getPayoff(myStrategy, otherStrategy);
		assertEquals(falseTrue, payoff, 0.01);
	}

	@Test
	public void getPayoffByStrategyBB() {

		game = new Game(trueTrue, trueFalse, falseTrue, falseFalse);
		boolean myStrategy = false;
		boolean otherStrategy = false;
		double payoff = game.getPayoff(myStrategy, otherStrategy);
		assertEquals(falseFalse, payoff, 0.01);
	}
	
	@BeforeEach
	public void setUpMocks() {

		Bag bag = new Bag();
		bag.add(neigh1);
		bag.add(neigh2);
		bag.add(neigh3);

		Mockito.when(agent.getNeighbourhood()).thenReturn(bag);

		Mockito.when(game.getPayoff(true, true)).thenReturn(trueTrue);

		// unnecessary stabbing for current tests
		Mockito.when(game.getPayoff(true, false)).thenReturn(trueFalse);
		Mockito.when(game.getPayoff(false, true)).thenReturn(falseTrue);
		Mockito.when(game.getPayoff(false, false)).thenReturn(falseFalse);
	}

	@Test
	public void getPayoffByAgentAllTrue() {
				
		boolean agentStrategy = true;
		boolean strategy1 = true;
		boolean strategy2 = true;
		boolean strategy3 = true;		
		
		Mockito.when(agent.getStrategy()).thenReturn(agentStrategy);
		Mockito.when(neigh1.getStrategy()).thenReturn(strategy1);
		Mockito.when(neigh2.getStrategy()).thenReturn(strategy2);
		Mockito.when(neigh3.getStrategy()).thenReturn(strategy3);
		
		double payoff = game.getPayoff(agent);
		Mockito.verify(game, Mockito.times(3)).getPayoff(agentStrategy, true);
		Mockito.verify(game, Mockito.times(0)).getPayoff(agentStrategy, false);
		Mockito.verify(game, Mockito.times(0)).getPayoff(false, false);	
		double expectedPayoff = trueTrue * 3;
		assertEquals(expectedPayoff, payoff, 0.01);
	}

	@Test
	public void getPayoffByAgentAllFalse() {

		boolean agentStrategy = false;
		boolean strategy1 = false;
		boolean strategy2 = false;
		boolean strategy3 = false;

		Mockito.when(agent.getStrategy()).thenReturn(agentStrategy);
		Mockito.when(neigh1.getStrategy()).thenReturn(strategy1);
		Mockito.when(neigh2.getStrategy()).thenReturn(strategy2);
		Mockito.when(neigh3.getStrategy()).thenReturn(strategy3);

		double payoff = game.getPayoff(agent);
		Mockito.verify(game, Mockito.times(3)).getPayoff(agentStrategy, false);
		Mockito.verify(game, Mockito.times(0)).getPayoff(agentStrategy, true);
		Mockito.verify(game, Mockito.times(0)).getPayoff(true, true);
		double expectedPayoff = falseFalse * 3;
		assertEquals(expectedPayoff, payoff, 0.01);

	}
	
	@Test
	public void getPayoffByAgentMixed() {
		boolean agentStrategy = true;
		boolean strategy1 = true;
		boolean strategy2 = false;
		boolean strategy3 = false;

		Mockito.when(agent.getStrategy()).thenReturn(agentStrategy);
		Mockito.when(neigh1.getStrategy()).thenReturn(strategy1);
		Mockito.when(neigh2.getStrategy()).thenReturn(strategy2);
		Mockito.when(neigh3.getStrategy()).thenReturn(strategy3);

		double payoff = game.getPayoff(agent);
		Mockito.verify(game, Mockito.times(1)).getPayoff(agentStrategy, true);
		Mockito.verify(game, Mockito.times(2)).getPayoff(agentStrategy, false);
		Mockito.verify(game, Mockito.times(0)).getPayoff(false, false);
		double expectedPayoff = trueTrue + trueFalse * 2;
		assertEquals(expectedPayoff, payoff, 0.01);

	}

}
