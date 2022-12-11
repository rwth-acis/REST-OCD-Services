package i5.las2peer.services.ocd.cooperation.data.simulation;

import java.util.List;

import javax.persistence.*;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentData {

	//ArangoDB
	public static final String collectionName = "agentdata";
	public static final String simulationDatasetKeyColumnName = "SIMULATION_DATASET_KEY";
	public static final String cooperativityColumnName = "COOPERATIVITY";
	public static final String wealthColumnName = "WEALTH";
	public static final String finalStrategyColumnName = "FINAL_STRATEGY";
	public static final String finalPayoffColumnName = "FINAL_PAYOFF";


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


	/**
	 * Update column values to be stored in the database.
	 * @param bd       Document holding updated values.
	 * @return         Document with updated values.
	 */
	public BaseDocument updateDocument(BaseDocument bd){
		//bd.addAttribute(simulationDatasetKeyColumnName, this.simulationDatasetKey); //TODO: DELETE
		bd.addAttribute(cooperativityColumnName, this.cooperativity);
		bd.addAttribute(wealthColumnName, this.wealth);
		bd.addAttribute(finalStrategyColumnName, this.finalStrategy);
		bd.addAttribute(finalPayoffColumnName, this.finalPayoff);
		return bd;
	}

	// Persistence Methods
	public void persist(ArangoDatabase db, String transId) {
		ArangoCollection collection = db.collection(collectionName);
		DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);
		BaseDocument bd = new BaseDocument();
		updateDocument(bd);
		collection.insertDocument(bd, createOptions);
		//this.key = bd.getKey(); // if key is assigned before inserting (line above) the value is null
	}

	public void updateDB(ArangoDatabase db, String transId) {
		DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);

		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		updateDocument(bd);
		//collection.updateDocument(this.key, bd, updateOptions);
	}


	public static AgentData load(String key, ArangoDatabase db, String transId) {
		AgentData agentData = new AgentData();
		ArangoCollection collection = db.collection(collectionName);

		BaseDocument bd = collection.getDocument(key, BaseDocument.class);
		if (bd != null) {
			agentData.setCooperativity((double) bd.getAttribute(cooperativityColumnName));
			agentData.setWealth((double) bd.getAttribute(wealthColumnName));
			agentData.setFinalStrategy((boolean) bd.getAttribute(finalStrategyColumnName));
			agentData.setFinalPayoff((double) bd.getAttribute(finalPayoffColumnName));

		}
		else {
			System.out.println("AgentData with key " + key + " not found.");
		}
		return agentData;
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
