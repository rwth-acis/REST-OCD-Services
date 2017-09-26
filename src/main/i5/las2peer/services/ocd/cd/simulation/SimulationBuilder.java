package i5.las2peer.services.ocd.cd.simulation;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.api.exceptions.ServiceInvocationException;
import i5.las2peer.services.ocd.cd.data.simulation.AgentData;
import i5.las2peer.services.ocd.cd.data.simulation.GroupType;
import i5.las2peer.services.ocd.cd.data.simulation.Parameters;
import i5.las2peer.services.ocd.cd.data.simulation.SimulationDataset;
import i5.las2peer.services.ocd.cd.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.cd.simulation.dynamic.Dynamic;
import i5.las2peer.services.ocd.cd.simulation.dynamic.DynamicFactory;
import i5.las2peer.services.ocd.cd.simulation.dynamic.DynamicType;
import i5.las2peer.services.ocd.cd.simulation.game.Game;
import i5.las2peer.services.ocd.cd.simulation.game.GameFactory;
import i5.las2peer.services.ocd.cd.simulation.game.GameType;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphProcessor;
import i5.las2peer.services.ocd.graphs.GraphType;
import sim.field.network.Network;
import sim.util.Bag;
import y.base.Edge;
import y.base.Node;

/**
 * Provides the interface to start simulations from the service.
 */
public class SimulationBuilder {

	DynamicFactory dynamicFactory;
	GameFactory gameFactory;

	Game game;
	Dynamic dynamic;
	CustomGraph network;
	int graphId;

	int iterations;

	/////// Constructor ////////

	public SimulationBuilder() {

		this.dynamicFactory = new DynamicFactory();
		this.gameFactory = new GameFactory();
	}

	public SimulationBuilder(DynamicFactory dynamicFactory, GameFactory gameFactory) {

		this.dynamicFactory = dynamicFactory;
		this.gameFactory = gameFactory;
	}

	/////// Simulation Series ////////

	public void setParameters(Parameters parameters) {

		try {
			setGameParameters(parameters);
			setDynamicParameters(parameters);
			iterations = parameters.getIterations();

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public Game setGameParameters(Parameters parameters) {

		GameType gameType = parameters.getGame();

		if (gameType.equals(GameType.PRISONERS_DILEMMA_COST) || gameType.equals(GameType.CHICKEN_COST)) {
			double benefit = parameters.getBenefit();
			double cost = parameters.getCost();
			game = gameFactory.build(gameType, cost, benefit);
			return game;
		}

		double payoffAA = parameters.getPayoffCC();
		double payoffAB = parameters.getPayoffCD();
		double payoffBA = parameters.getPayoffDC();
		double payoffBB = parameters.getPayoffDD();

		gameType = GameType.getGameType(payoffAA, payoffAB, payoffBA, payoffBB);
		if (gameType.equals(GameType.INVALID))
			throw new IllegalArgumentException("invalid game type");

		game = gameFactory.build(payoffAA, payoffAB, payoffBA, payoffBB);
		return game;
	}

	public Dynamic setDynamicParameters(Parameters parameters) {

		DynamicType dynamicType = parameters.getDynamic();
		if (dynamicType == null || dynamicType.equals(DynamicType.UNKNOWN))
			throw new IllegalArgumentException("invalid dynamic type");

		double[] values = parameters.getDynamicValues();
		dynamic = dynamicFactory.build(dynamicType, values);
		return dynamic;
	}

	public void setNetwork(CustomGraph network) {

		if (network == null)
			throw new IllegalArgumentException("no network");

		this.network = network;
	}

	public void setIterations(int value) {

		if (value < 1)
			throw new IllegalArgumentException("negative number of iterations");

		this.iterations = value;
	}

	/**
	 * Checks if all informations are available to create a legal simulation
	 * 
	 * @throws IllegalStateException
	 */
	protected void validate() throws IllegalStateException {

		if (this.game == null)
			throw new IllegalStateException("no game defined");

		if (this.dynamic == null)
			throw new IllegalStateException("no dynamic defined");

		if (this.network == null)
			throw new IllegalStateException("no network defined");
	}

	public SimulationSeries simulate() throws IllegalStateException, ServiceInvocationException {

		try {
			validate();
		} catch (IllegalStateException e) {
			throw e;
		}

		Game game = this.game;
		Dynamic dynamic = this.dynamic;
		CustomGraph graph = this.network;
		Network network = buildNetwork(graph);

		int iterations = this.iterations;
		if (iterations < 1)
			iterations = 1;

		List<SimulationDataset> datasets = new ArrayList<>(iterations);
		Simulation simulation = new Simulation(System.currentTimeMillis(), network, game, dynamic);

		for (int i = 0; i < iterations; i++) {

			System.out.print(" " + i);
			simulation.start();
			do
				if (!simulation.schedule.step(simulation))
					break;
			while (simulation.schedule.getSteps() < 5000);
			SimulationDataset dataset = getSimulationData(simulation);
			dataset.setName(String.valueOf(i));
			dataset.evaluate();
			datasets.add(dataset);
			simulation.finish();
		}

		Parameters parameters = new Parameters();
		parameters.setDynamic(dynamic.getDynamicType());
		parameters.setGame(game.getGameType());
		parameters.setPayoffCC(game.getPayoffAA());
		parameters.setPayoffCD(game.getPayoffAB());
		parameters.setPayoffDC(game.getPayoffBA());
		parameters.setPayoffDD(game.getPayoffBB());
		parameters.setGraphId(graph.getId());

		SimulationSeries series = new SimulationSeries(parameters, datasets);
		series.evaluate();
		return (series);
	}

	/**
	 * Returns the results of a simulation as {@link SimulationDataset}
	 * 
	 * @return SimulationData
	 */
	public SimulationDataset getSimulationData(Simulation simulation) {

		BreakCondition breakCondition = simulation.getBreakCondition();
		DataRecorder recorder = simulation.getDataRecorder();

		Bag agents = simulation.getAgents();
		int size = agents.size();
		List<AgentData> agentDataList = new ArrayList<AgentData>(size);
		for (int agentId = 0; agentId < size; agentId++) {
			Agent agent = (Agent) agents.get(agentId);
			AgentData data = new AgentData(agent.getStrategiesList(), agent.getPayoffList(),
					breakCondition.getWindow());

			agentDataList.add(data);
		}

		double cooperativity = recorder.getAverageCooperativity(breakCondition.getWindow());
		double wealth = recorder.getAverageWealth(breakCondition.getWindow());

		SimulationDataset simulationData = new SimulationDataset(recorder.getCooperationValues(),
				recorder.getPayoffValues(), agentDataList, cooperativity, wealth);
		return simulationData;
	}

	/////// Simulation Series Group ////////

	public void setGameParameters(Parameters parameters, GroupType game, double scale) {

		switch (game) {
		case Rescaled_PD:
		default:
			parameters.setGame(GameType.PRISONERS_DILEMMA);
			parameters.setPayoffCC(scale);
			parameters.setPayoffCD(1);
			parameters.setPayoffDC(0);
			parameters.setPayoffDD(0);
		}
	}

	public void setDynamicParameters(Parameters parameters, DynamicType dynamic) {

		switch (dynamic) {
		case REPLICATOR:
		default:
			parameters.setDynamic(DynamicType.REPLICATOR);
			parameters.setDynamicValue(1.5);
		}
	}

	/////// Initialize Network ////////

	/**
	 * Transform a service network representation into a MASON compatible
	 * network.
	 * 
	 * @param graph
	 *            network as {@link CustomGraph}
	 * @return network as {@link Network}
	 */
	protected Network buildNetwork(CustomGraph graph) {

		if (graph.getTypes().isEmpty()) {
			GraphProcessor proc = new GraphProcessor();
			proc.determineGraphTypes(graph);
		}

		if (graph.getTypes().contains(GraphType.SELF_LOOPS))
			throw new IllegalArgumentException("Self loops not supported");

		Network network = new Network(false);
		List<Agent> agents = new ArrayList<>();

		for (Node node : graph.getNodeArray()) {
			Agent agent = new Agent(node.index());
			agents.add(node.index(), agent);
			network.addNode(agent);
		}

		for (Edge edge : graph.getEdgeArray()) {
			int source = edge.source().index();
			int target = edge.target().index();
			if (network.getEdge(agents.get(source), agents.get(target)) == null)
				network.addEdge(agents.get(source), agents.get(target), true);
		}
		return network;
	}

}
