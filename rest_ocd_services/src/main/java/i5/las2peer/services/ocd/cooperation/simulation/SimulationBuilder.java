package i5.las2peer.services.ocd.cooperation.simulation;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.services.ocd.cooperation.data.simulation.AgentData;
import i5.las2peer.services.ocd.cooperation.data.simulation.GroupType;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationDataset;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeries;
import i5.las2peer.services.ocd.cooperation.data.simulation.SimulationSeriesParameters;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.Dynamic;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.DynamicFactory;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.DynamicType;
import i5.las2peer.services.ocd.cooperation.simulation.game.Game;
import i5.las2peer.services.ocd.cooperation.simulation.game.GameFactory;
import i5.las2peer.services.ocd.cooperation.simulation.game.GameType;
import i5.las2peer.services.ocd.cooperation.simulation.termination.Condition;
import i5.las2peer.services.ocd.cooperation.simulation.termination.ConditionFactory;
import i5.las2peer.services.ocd.cooperation.simulation.termination.ConditionType;
import i5.las2peer.services.ocd.cooperation.simulation.termination.StationaryStateCondition;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import sim.field.network.Network;
import sim.util.Bag;
import y.base.Edge;
import y.base.Node;

/**
 * Provides the interface to start simulations from the service.
 */
public class SimulationBuilder {

	DynamicFactory dynamicFactory = new DynamicFactory();;
	GameFactory gameFactory = new GameFactory();;
	ConditionFactory conditionFactory = new ConditionFactory();;

	Game game;
	Dynamic dynamic;
	CustomGraph network;
	Condition condition;
	int graphId;

	int iterations;
	String name;

	/////// Constructor ////////

	public SimulationBuilder() {

		this.dynamicFactory = new DynamicFactory();
		this.gameFactory = new GameFactory();
		this.conditionFactory = new ConditionFactory();
	}

	public SimulationBuilder(DynamicFactory dynamicFactory, GameFactory gameFactory) {

		this.dynamicFactory = dynamicFactory;
		this.gameFactory = gameFactory;
	}

	/////// Simulation Series ////////

	public void setParameters(SimulationSeriesParameters parameters) {

		try {
			setGameParameters(parameters);
			setDynamicParameters(parameters);
			setConditionParameters(parameters);
			setIterations(parameters.getIterations());
			setName(parameters.getName());

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Set the simulation parameters regarding the game.
	 * 
	 * @param parameters
	 * @return Game
	 */
	public Game setGameParameters(SimulationSeriesParameters parameters) {

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

	/**
	 * Set the simulation parameters regarding the dynamic.
	 * 
	 * @param parameters
	 * @return Dynamic
	 */
	public Dynamic setDynamicParameters(SimulationSeriesParameters parameters) {

		DynamicType dynamicType = parameters.getDynamic();
		if (dynamicType == null || dynamicType.equals(DynamicType.UNKNOWN))
			throw new IllegalArgumentException("invalid dynamic type");

		double[] values = parameters.getDynamicValues();
		dynamic = dynamicFactory.build(dynamicType, values);
		return dynamic;
	}

	/**
	 * Set the simulation parameters regarding the break condition.
	 * 
	 * @param parameters
	 * @return Condition
	 */
	public Condition setConditionParameters(SimulationSeriesParameters parameters) {

		ConditionType conditionType = parameters.getCondition();
		if (conditionType == null || conditionType.equals(ConditionType.UNKNOWN))
			throw new IllegalArgumentException("invalid condition type");

		int[] values = parameters.getConditionValues();
		condition = conditionFactory.build(conditionType, values);
		return condition;
	}

	/**
	 * Set the Graph
	 * 
	 * @param network
	 */
	public void setNetwork(CustomGraph network) {

		if (network == null)
			throw new IllegalArgumentException("no network");

		this.network = network;
	}

	/**
	 * Set the number of simulations
	 * 
	 * @param number
	 *            of simulations
	 */
	public void setIterations(int value) {

		if (value < 1)
			throw new IllegalArgumentException("negative number of iterations");

		this.iterations = value;
	}
	
	/**
	 * Set the name of the simulation
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
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

		if (this.condition == null)
			throw new IllegalStateException("no break condition defined");
	}

	/**
	 * Start a simulation series
	 * 
	 * @return SimulationSeries
	 * @throws IllegalStateException
	 * @throws ServiceInvocationException
	 */
	public SimulationSeries simulate() throws IllegalStateException, ServiceInvocationException {

		try {
			validate();
		} catch (IllegalStateException e) {
			throw e;
		}

		Game game = this.game;
		Dynamic dynamic = this.dynamic;
		CustomGraph graph = this.network;
		Condition condition = this.condition;
		Network network = buildNetwork(graph);

		int iterations = this.iterations;
		if (iterations < 1)
			iterations = 10;

		if (condition instanceof StationaryStateCondition) {
			StationaryStateCondition stCondition = (StationaryStateCondition) condition;
			stCondition.setThreshold(1 / (Math.sqrt(network.getAllNodes().size())));
			condition = stCondition;
		}

		if(this.name == null)
			this.name = "";	
		
		List<SimulationDataset> datasets = new ArrayList<>();
		Simulation simulation = new Simulation(System.currentTimeMillis(), network, game, dynamic, condition);

		System.out.println("simulate");
		System.out.println(simulation.getBreakCondition().getMaxIterations());
		System.out.println(simulation.getBreakCondition().toString());
		for (int i = 0; i < iterations; i++) {

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

		SimulationSeriesParameters parameters = new SimulationSeriesParameters();
		parameters.setDynamic(dynamic.getDynamicType());
		parameters.setGame(game.getGameType());
		parameters.setPayoffCC(game.getPayoffAA());
		parameters.setPayoffCD(game.getPayoffAB());
		parameters.setPayoffDC(game.getPayoffBA());
		parameters.setPayoffDD(game.getPayoffBB());
		parameters.setGraphId(graph.getId());
		parameters.setIterations(iterations);
		parameters.setGraphName(graph.getName());
		parameters.setMaxIterations(condition.getMaxIterations());
		parameters.setTimeWindow(condition.getWindow());

		SimulationSeries series = new SimulationSeries(parameters, datasets);
		series.setName(name);
		series.evaluate();
		return (series);
	}

	/**
	 * Returns the results of a simulation as {@link SimulationDataset}
	 * 
	 * @return SimulationData
	 */
	public SimulationDataset getSimulationData(Simulation simulation) {

		Condition breakCondition = simulation.getBreakCondition();
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

	public void setGameParameters(SimulationSeriesParameters parameters, GroupType game, double scale) {

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

	public void setDynamicParameters(SimulationSeriesParameters parameters, DynamicType dynamic) {

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
