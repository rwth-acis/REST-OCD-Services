package i5.las2peer.services.ocd.cd.data.simulation;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import i5.las2peer.services.ocd.cd.data.table.Table;
import i5.las2peer.services.ocd.cd.data.table.TableRow;
import i5.las2peer.services.ocd.graphs.Community;

/**
 * Simulation Data
 * 
 * Objects of this class represent the data collected by simulation series The
 * objects can be stored
 * 
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulationDataset extends SimulationAbstract {

	/////////////// Entity Fields ///////////////

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn
	private List<AgentData> agentData;
	
	@Basic
	private double finalCooperationValue;
	
	@Basic
	private double finalPayoffValue;
	
	@Basic
	private double iterations;
	
	@ElementCollection
	private List<Double> cooperationValues;

	@ElementCollection
	private List<Double> payoffValues;

	@Transient
	private boolean stable;

	/////////////// Constructor ///////////////
	
	/**
	 * Creates a empty instance, that is used for persistence and testing purposes.
	 */
	public SimulationDataset() {

	}
	
	
	public SimulationDataset(List<Double> cooperationValues, List<Double> payoffValues, List<AgentData> agentDataList, double cooperativity, double wealth) {

		this.cooperationValues = cooperationValues;
		this.payoffValues = payoffValues;
		this.agentData = agentDataList;
						
		this.finalCooperationValue =  cooperativity;
		this.finalPayoffValue =  wealth;
	}
	
	public SimulationDataset(List<Double> cooperationValues, List<Double> payoffValues, List<AgentData> agentDataList) {

		this.cooperationValues = cooperationValues;
		this.payoffValues = payoffValues;
		this.agentData = agentDataList;
						
		this.finalCooperationValue =  cooperationValues.get(cooperationValues.size() - 1);
		this.finalPayoffValue =  payoffValues.get(payoffValues.size() - 1);
	}

	/////////////// Getter ///////////////

	@JsonProperty
	public List<Double> getCooperationValues() {
		return this.cooperationValues;
	}

	@JsonProperty
	public List<Double> getPayoffValues() {
		return this.payoffValues;
	}

	@JsonProperty
	public List<AgentData> getAgentData() {
		return this.agentData;
	}

	@JsonProperty
	public double getFinalCooperationValue() {
		return this.finalCooperationValue;
	}
	
	@JsonProperty
	public double getIterations() {
		return this.iterations;
	}

	@JsonProperty
	public double getFinalPayoffValue() {
		return this.finalPayoffValue;
	}

	@JsonIgnore
	public boolean isStable() {
		return stable;
	}
	
	////// Setter /////

	public void setAgentData(List<AgentData> agentList) {
		this.agentData = agentList;
	}

	public void setCooperationValues(List<Double> cooperationValues) {
		this.cooperationValues = cooperationValues;
	}

	public void setPayoffValues(List<Double> payoffValues) {
		this.payoffValues = payoffValues;
	}

	public void setStable(boolean stable) {
		this.stable = stable;
	}
	
	public void setFinalCooperationValue(double finalCooperationValue) {
		this.finalCooperationValue = finalCooperationValue;
	}

	public void setFinalPayoffValue(double finalPayoffValue) {
		this.finalPayoffValue = finalPayoffValue;
	}	
			
	public void setIterations(int iterations) {
		this.iterations = iterations;
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

	@JsonIgnore
	public boolean getAgentStrategy(int agentId) {
		return getAgentData().get(agentId).getFinalStrategy();
	}
	
	@JsonIgnore
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

		Evaluation coopEvaluation = getCooperationEvaluation();
		Evaluation payoffEvaluation = getPayoffEvaluation();

		TableRow line = new TableRow();
		line.add("#C");
		line.add(coopEvaluation.toHeadLine().suffix("#C"));
		line.add("#P");
		line.add(payoffEvaluation.toHeadLine().suffix("#P"));
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

}
