package i5.las2peer.services.ocd.cooperation.data.simulation;

import java.util.ArrayList;
import java.util.List;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import i5.las2peer.services.ocd.cooperation.data.table.Table;
import i5.las2peer.services.ocd.cooperation.data.table.TableRow;
import i5.las2peer.services.ocd.graphs.Community;

/**
 * Simulation Data
 *
 * Objects of this class represent the data collected by simulation series The
 * objects can be stored
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulationDataset extends SimulationAbstract {

	@JsonProperty
	private List<AgentData> agentData;

	@JsonProperty
	private double finalCooperationValue;

	@JsonProperty
	private double finalPayoffValue;

	@JsonProperty
	private double iterations;

	@JsonProperty
	private List<Double> cooperationValues;

	@JsonProperty
	private List<Double> payoffValues;

	@JsonProperty
	private boolean stable;

	/////////////// Constructor ///////////////

	/**
	 * Creates a empty instance, that is used for persistence and testing
	 * purposes.
	 */
	public SimulationDataset() {

	}

	public SimulationDataset(List<Double> cooperationValues, List<Double> payoffValues, List<AgentData> agentDataList,
			double cooperativity, double wealth) {

		this.cooperationValues = cooperationValues;
		this.payoffValues = payoffValues;
		this.agentData = agentDataList;

		this.finalCooperationValue = cooperativity;
		this.finalPayoffValue = wealth;
	}

	public SimulationDataset(List<Double> cooperationValues, List<Double> payoffValues, List<AgentData> agentDataList) {

		this.cooperationValues = cooperationValues;
		this.payoffValues = payoffValues;
		this.agentData = agentDataList;

		this.finalCooperationValue = cooperationValues.get(cooperationValues.size() - 1);
		this.finalPayoffValue = payoffValues.get(payoffValues.size() - 1);
	}

	/////////// Getters/Setters ////////////


	public List<AgentData> getAgentData() {
		return agentData;
	}

	public void setAgentData(List<AgentData> agentData) {
		this.agentData = agentData;
	}

	public double getFinalCooperationValue() {
		return finalCooperationValue;
	}

	public void setFinalCooperationValue(double finalCooperationValue) {
		this.finalCooperationValue = finalCooperationValue;
	}

	public double getFinalPayoffValue() {
		return finalPayoffValue;
	}

	public void setFinalPayoffValue(double finalPayoffValue) {
		this.finalPayoffValue = finalPayoffValue;
	}

	public double getIterations() {
		return iterations;
	}

	public void setIterations(double iterations) {
		this.iterations = iterations;
	}

	public List<Double> getCooperationValues() {
		return cooperationValues;
	}

	public void setCooperationValues(List<Double> cooperationValues) {
		this.cooperationValues = cooperationValues;
	}

	public List<Double> getPayoffValues() {
		return payoffValues;
	}

	public void setPayoffValues(List<Double> payoffValues) {
		this.payoffValues = payoffValues;
	}

	public boolean isStable() {
		return stable;
	}

	public void setStable(boolean stable) {
		this.stable = stable;
	}


	//////////// Methods ////////////

	@Override
	public void evaluate() {

		setCooperationEvaluation(new Evaluation(getCooperationValues()));
		setPayoffEvaluation(new Evaluation(getPayoffValues()));
		setIterations(generations());

	}

	public int generations() {
		return getCooperationValues().size();
	}

	public int fill(int newSize) {
		int oldSize = getCooperationValues().size();
		if (newSize > oldSize) {
			double last = getFinalCooperationValue();
			for (int i = oldSize; i < newSize; i++) {
				cooperationValues.add(last);
			}
			last = getFinalPayoffValue();
			for (int i = payoffValues.size(); i < newSize; i++) {
				payoffValues.add(last);
			}
		}
		return newSize;
	}

	public double[] getCommunityCooperationValues(List<Community> communityList) {

		int communityCount = communityList.size();
		double[] values = new double[communityCount];

		for (int communityId = 0; communityId < communityCount; communityId++) {
			List<Integer> memberList = communityList.get(communityId).getMemberIndices();

			try {
				values[communityId] = getCommunityCooperationValue(memberList);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("invalid communityList");
			}
		}
		return values;
	}

	public double getCommunityCooperationValue(List<Integer> memberList) {

		int agentCount = agentCount();
		int memberCount = memberList.size();

		double cooperativitySum = 0;
		for (int i = 0; i < memberCount; i++) {
			int memberId = memberList.get(i);

			if (memberId >= agentCount || memberId < 0)
				throw new IllegalArgumentException("invalid memberList");

			cooperativitySum += getAgentCooperativity(memberId);
		}

		return cooperativitySum / memberCount;

	}


	public boolean getAgentStrategy(int agentId) {
		return getAgentData().get(agentId).getFinalStrategy();
	}


	public double getAgentCooperativity(int agentId) {
		return getAgentData().get(agentId).getCooperativity();
	}

	public int agentCount() {
		if (this.getAgentData() == null)
			return 0;
		return this.getAgentData().size();
	}

	////////////// Print Data /////////////

	@Override
	public TableRow toTableLine() {
		
		if(getCooperationEvaluation() == null || getPayoffEvaluation() == null)
			this.evaluate();
		
		Evaluation coopEvaluation = getCooperationEvaluation();
		Evaluation payoffEvaluation = getPayoffEvaluation();
		
		TableRow line = new TableRow();
		line.add(getFinalCooperationValue());
		line.add(coopEvaluation.toTableLine());
		line.add(getFinalPayoffValue());
		line.add(payoffEvaluation.toTableLine());
		line.add(getIterations());

		return line;
	}

	public TableRow toHeadLine() {

		TableRow line = new TableRow();
		line.add("#C");
		line.add(Evaluation.toHeadLine().suffix("#C"));
		line.add("#P");
		line.add(Evaluation.toHeadLine().suffix("#P"));
		line.add("Iterations");

		return line;
	}

	@Override
	public Table toTable() {

		Table table = new Table();

		List<Double> values = getCooperationValues();
		for (int i = 0; i < values.size(); i++) {
			double value = values.get(i);
			table.add(new TableRow().add(value));
		}
		return table;
	}

	/**
	 * Helper method to convert object representation of a list
	 * returned in a query into an array list
	 * @param listToParse         Object to parse as a list
	 * @return                    ArrayList representation of the input object
	 */
	public static ArrayList<Double> documentToArrayList(Object listToParse){
		ObjectMapper objectMapper = new ObjectMapper();
		ArrayList<Double> res;
		res = objectMapper.convertValue(listToParse, ArrayList.class);
		return res;
	}

	@Override
	public String toString() {
		return "SimulationDataset{" +
				", finalCooperationValue=" + finalCooperationValue +
				", finalPayoffValue=" + finalPayoffValue +
				", iterations=" + iterations +
				", cooperationValues=" + cooperationValues +
				", payoffValues=" + payoffValues +
				", stable=" + stable +
				'}';
	}

}
