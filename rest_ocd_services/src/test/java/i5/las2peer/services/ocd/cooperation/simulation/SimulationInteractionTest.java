package i5.las2peer.services.ocd.cooperation.simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.cooperation.simulation.dynamic.Dynamic;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.DynamicFactory;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.DynamicType;
import i5.las2peer.services.ocd.cooperation.simulation.game.Game;
import i5.las2peer.services.ocd.cooperation.simulation.game.GameFactory;
import i5.las2peer.services.ocd.cooperation.simulation.game.GameType;
import i5.las2peer.services.ocd.cooperation.simulation.termination.Condition;
import i5.las2peer.services.ocd.cooperation.simulation.termination.ConditionFactory;
import i5.las2peer.services.ocd.cooperation.simulation.termination.ConditionType;
import sim.field.network.Network;


/**
 * It is not possible to determine the exact simulation process because of the
 * random factors. However, we test if the simulation terminate with a possible
 * result.
 */
public class SimulationInteractionTest {

	private Network network;

	Agent agent0;
	Agent agent1;
	Agent agent2;
	Agent agent3;
	Agent agent4;
	Agent agent5;

	@BeforeEach
	public void setupNetwork() {

		network = new Network();

		agent0 = new Agent(0);
		agent1 = new Agent(1);
		agent2 = new Agent(2);
		agent3 = new Agent(3);
		agent4 = new Agent(4);
		agent5 = new Agent(5);

		network.addNode(agent0);
		network.addNode(agent1);
		network.addNode(agent2);
		network.addNode(agent3);

		network.addEdge(agent0, agent1, 1);
		network.addEdge(agent0, agent2, 1);
		network.addEdge(agent0, agent3, 1);
		network.addEdge(agent1, agent2, 1);
		network.addEdge(agent1, agent3, 1);
		network.addEdge(agent2, agent3, 1);

	}

	@Test
	public void ImitationPrisonersDilemmaDefection() {

		Game game = GameFactory.getInstance().build(GameType.PRISONERS_DILEMMA, 1, 2);
		Dynamic dynamic = DynamicFactory.getInstance().build(DynamicType.UNCONDITIONAL_IMITATION);
		Condition condition = new ConditionFactory().build(ConditionType.STATIONARY_STATE);

		Simulation simulation = new Simulation(System.currentTimeMillis(), network, game, dynamic, condition);
		simulation.start();
		do
			if (!simulation.schedule.step(simulation))
				break;
		while (simulation.schedule.getSteps() < 5000);
		assertTrue(((Double) simulation.getAveragePayoff()).equals(0.0)
				|| ((Double) simulation.getAveragePayoff()).equals(1.5));
		assertTrue(((Double) simulation.getCooperationValue()).equals(0.0)
				|| ((Double) simulation.getCooperationValue()).equals(1.0));

	}
	
	@Test
	public void ReplicatorPrisonersDilemmaDefection() {

		Game game = GameFactory.getInstance().build(GameType.PRISONERS_DILEMMA, 1.0, 2.0);
		Dynamic dynamic = DynamicFactory.getInstance().build(DynamicType.REPLICATOR);
		Condition condition = new ConditionFactory().build(ConditionType.STATIONARY_STATE);

		Simulation simulation = new Simulation(System.currentTimeMillis(), network, game, dynamic, condition);
		simulation.start();
		do
			if (!simulation.schedule.step(simulation))
				break;
		while (simulation.schedule.getSteps() < 5000);
		assertTrue(((Double) simulation.getAveragePayoff()).equals(0.0)
				|| ((Double) simulation.getAveragePayoff()).equals(1.5) || ((Double) simulation.getAveragePayoff()).equals(2.25));
		assertTrue(((Double) simulation.getCooperationValue()).equals(0.0)
				|| ((Double) simulation.getCooperationValue()).equals(0.5) || ((Double) simulation.getCooperationValue()).equals(0.25));

	}

}
