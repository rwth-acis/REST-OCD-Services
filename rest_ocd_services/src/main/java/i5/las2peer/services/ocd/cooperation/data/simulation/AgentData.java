package i5.las2peer.services.ocd.cooperation.data.simulation;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Class to hold agent related data
 */
public class AgentData {

	/**
	 * The simulation dataset this agent data belongs to
	 */
	private SimulationDataset simulationDataset;

	/**
	 * Used strategies of every generation
	 */
	private List<Boolean> strategies;
	
	/**
	 * Received payoff of every generation
	 */
	private List<Double> payoff;

	/**
	 * The cooperativity value of the agent. Averaged over the last strategies
	 */
	private double cooperativity;
	
	/**
	 * The wealth value of the agent. Averaged over the last payoff values.
	 */
	private double wealth;

	private boolean finalStrategy;

	private double finalPayoff;

	///// Constructor /////

	/**
	 * Creates a empty instance. Used for persistence and testing purposes.
	 */
	public AgentData() {

	}

	/**
	 * Creates a new instance with injected data.
	 * 
	 * @param strategies The used strategies during the simulation
	 * @param payoff The received payoff values during the simulation
	 * @param timeWindow The number of generations to compute the cooperativity 
	 */
	public AgentData(List<Boolean> strategies, List<Double> payoff, int timeWindow) {
		setStrategies(strategies);
		setPayoff(payoff);

		double cooperativity = 0.0;
		double wealth = 0.0;

		int size = strategies.size();
		for (int i = size - 1; i >= size - timeWindow; i--) {
			if (strategies.get(i) == true)
				cooperativity++;
			wealth += payoff.get(i);
		}
		setCooperativity(cooperativity / timeWindow);
		setWealth( wealth/ timeWindow);
	}

	///// Getter /////

	@JsonIgnore
	public List<Boolean> getStrategies() {
		return strategies;
	}

	@JsonProperty
	public boolean getFinalStrategy() {
		return this.finalStrategy;
	}

	@JsonIgnore
	public List<Double> getPayoff() {
		return payoff;
	}

	@JsonProperty
	public double getFinalPayoff() {
		return this.finalPayoff;
	}

	@JsonProperty
	public double getCooperativity() {
		return cooperativity;
	}

	@JsonProperty
	public double getWealth() {
		return wealth;
	}

	///// Setter //////

	public void setStrategies(List<Boolean> strategies) {
		this.strategies = strategies;
		finalStrategy = strategies.get(strategies.size() - 1);
	}

	public void setPayoff(List<Double> payoff) {
		this.payoff = payoff;
		finalPayoff = payoff.get(payoff.size() - 1);
	}

	public void setFinalStrategy(boolean strategy) {
		this.finalStrategy = strategy;
	}

	public void setFinalPayoff(double payoff) {
		this.finalPayoff = payoff;
	}

	public void setCooperativity(double cooperativity) {
		this.cooperativity = cooperativity;
	}

	public void setWealth(double wealth) {
		this.wealth = wealth;
	}

	public SimulationDataset getSimulationDataset() {
		return simulationDataset;
	}

	public void setSimulationDataset(SimulationDataset simulationDataset) {
		this.simulationDataset = simulationDataset;
	}

	public boolean isFinalStrategy() {
		return finalStrategy;
	}



	@Override
	public String toString() {
		return "AgentData{" +
				", simulationDataset=" + simulationDataset +
				", strategies=" + strategies +
				", payoff=" + payoff +
				", cooperativity=" + cooperativity +
				", wealth=" + wealth +
				", finalStrategy=" + finalStrategy +
				", finalPayoff=" + finalPayoff +
				'}';
	}
}
