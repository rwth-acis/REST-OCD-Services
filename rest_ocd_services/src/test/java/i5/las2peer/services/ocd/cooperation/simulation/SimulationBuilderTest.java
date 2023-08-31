package i5.las2peer.services.ocd.cooperation.simulation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesParameters;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.Dynamic;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.DynamicType;
import i5.las2peer.services.ocd.cooperation.simulation.game.Game;
import i5.las2peer.services.ocd.cooperation.simulation.game.GameType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;

import sim.field.network.Network;
import sim.util.Bag;
import org.graphstream.graph.Node;
import java.util.UUID;


public class SimulationBuilderTest {
	

	
	SimulationBuilder simulationBuilder;
	SimulationSeriesParameters parameters;
	
	@Test
	public void setGameInvalidParameters() {

		simulationBuilder = new SimulationBuilder();		
		parameters = new SimulationSeriesParameters();
		parameters.setPayoffCC(0);
		parameters.setPayoffCD(0);
		parameters.setPayoffDC(0);
		parameters.setPayoffDD(0);

		assertThrows(IllegalArgumentException.class, () -> simulationBuilder.setGameParameters(parameters));

	}
	
	@Test
	public void setGameTest() {
		
		Game resultGame;
		double aa = 2;
		double ab = 0;
		double ba = 3;
		double bb = 1;		
		
		simulationBuilder = new SimulationBuilder();		
		parameters = new SimulationSeriesParameters();
		parameters.setPayoffCC(aa);
		parameters.setPayoffCD(ab);
		parameters.setPayoffDC(ba);
		parameters.setPayoffDD(bb);
		
		resultGame = simulationBuilder.setGameParameters(parameters);
		assertEquals(GameType.PRISONERS_DILEMMA, resultGame.getGameType());
		assertEquals(aa, resultGame.getPayoffAA(), 0.1);
		assertEquals(ab, resultGame.getPayoffAB(), 0.1);
		assertEquals(ba, resultGame.getPayoffBA(), 0.1);
		assertEquals(bb, resultGame.getPayoffBB(), 0.1);
	}
	
	@Test
	public void setDynamicUnknownDynamic() {

		simulationBuilder = new SimulationBuilder();		
		parameters = new SimulationSeriesParameters();
		parameters.setDynamic(DynamicType.UNKNOWN);

		assertThrows(IllegalArgumentException.class, () -> simulationBuilder.setDynamicParameters(parameters));

	}
	
	@Test
	public void setDynamicTest() {
		
		Dynamic resultDynamic;		
		
		simulationBuilder = new SimulationBuilder();		
		parameters = new SimulationSeriesParameters();
		parameters.setDynamic(DynamicType.REPLICATOR);
		parameters.setDynamicValue(1.2);
		
		resultDynamic = simulationBuilder.setDynamicParameters(parameters);		
		assertEquals(DynamicType.REPLICATOR, resultDynamic.getDynamicType());		
	}
	
		
	
	@Test
	public void buildNetworkTest() {
				
		SimulationBuilder simulationBuilder = new SimulationBuilder();
		CustomGraph graph = new CustomGraph();
		Node n0 = graph.addNode("n0");
		Node n1 = graph.addNode("n1");
		Node n2 = graph.addNode("n2");
		Node n3 = graph.addNode("n3");
		Node n4 = graph.addNode("n4");
		
		graph.addEdge(UUID.randomUUID().toString(), n0, n1);
		graph.addEdge(UUID.randomUUID().toString(), n1, n2);
		graph.addEdge(UUID.randomUUID().toString(), n3, n2);
		graph.addEdge(UUID.randomUUID().toString(), n4, n1);

		Network network = simulationBuilder.buildNetwork(graph);		
		assertNotNull(network);		
		Bag agents = network.getAllNodes();			
		assertEquals(5, agents.size());
		assertEquals(Agent.class, agents.get(0).getClass());
		assertEquals(Agent.class, agents.get(1).getClass());
		assertEquals(Agent.class, agents.get(2).getClass());
		assertEquals(Agent.class, agents.get(3).getClass());
		assertEquals(Agent.class, agents.get(4).getClass());		
		assertEquals(0, ((Agent) agents.get(0)).getNodeId());
		assertEquals(1, ((Agent) agents.get(1)).getNodeId());
		assertEquals(2, ((Agent) agents.get(2)).getNodeId());
		assertEquals(3, ((Agent) agents.get(3)).getNodeId());	
		assertEquals(4, ((Agent) agents.get(4)).getNodeId());
		assertEquals(1, network.getEdgesIn(agents.get(0)).size());
		assertEquals(3, network.getEdgesIn(agents.get(1)).size());
		assertEquals(2, network.getEdgesIn(agents.get(2)).size());
		assertEquals(1, network.getEdgesIn(agents.get(3)).size());
		assertEquals(1, network.getEdgesIn(agents.get(4)).size());
		
	}
	
	@Test
	public void buildNetworkSelfLoops() {

		SimulationBuilder simulationBuilder = new SimulationBuilder();
		CustomGraph graph = new CustomGraph();
		
		//@MaxKissgen Previously, SELF_LOOPS was only added to the Set returned from getTypes().
		//This set is not the internal list of types of a graph, and SELF_LOOPS would therefore not have been added to it and the SimulationBuilderTest would have failed.
		graph.addType(GraphType.SELF_LOOPS);

		Node n0 = graph.addNode("n0");
		Node n1 = graph.addNode("n1");
		Node n2 = graph.addNode("n2");

		graph.addEdge(UUID.randomUUID().toString(), n0, n1);
		graph.addEdge(UUID.randomUUID().toString(), n1, n1);
		graph.addEdge(UUID.randomUUID().toString(), n0, n2);

		assertThrows(IllegalArgumentException.class, () -> simulationBuilder.buildNetwork(graph));


	}
	
	
}
