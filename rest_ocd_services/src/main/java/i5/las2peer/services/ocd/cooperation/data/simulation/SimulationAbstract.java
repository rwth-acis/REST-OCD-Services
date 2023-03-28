package i5.las2peer.services.ocd.cooperation.data.simulation;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import i5.las2peer.services.ocd.cooperation.data.table.Table;
import i5.las2peer.services.ocd.cooperation.data.table.TableInterface;
import i5.las2peer.services.ocd.cooperation.data.table.TableRow;
import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * This is the super class of all classes that store data that was collected
 * from simulations.
 *
 */
public abstract class SimulationAbstract implements TableInterface {

	//ArangoDB
	public static final String nameColumnName = "NAME";
	public static final String userIdColumnName = "USER";
	public static final String cooperativiatyColumnName = "COOPERATIVIATY";
	public static final String wealthColumnName = "WEALTH";
	public static final String cooperationEvaluationColumnName = "COOPERATION_EVALUATION";
	public static final String payoffEvaluationColumnName = "PAYOFF_EVALUATION";
	public static final String generationEvaluationColumnName = "GENERATION_EVALUATION";
	public static final String graphKeyName = "GRAPH_KEY";

	////////// Entity Fields //////////

	/**
	 * System generated persistence key.
	 */
	private String key;

	/**
	 * The name of the simulation
	 */
	@JsonProperty
	private String name;

	/**
	 * The Id of the owning user
	 */
	private String userId;

	/**
	 * cooperativity the simulation
	 */
	private double cooperativiaty;

	/**
	 * Wealth the simulation
	 */
	private double wealth;

	/**
	 * Statistical evaluation of the cooperation values of the
	 * SimulationDatasets
	 */
	private Evaluation cooperationEvaluation;


	/**
	 * Statistical evaluation of the payoff values of the SimulationDatasets
	 */
	private Evaluation payoffEvaluation;


	/**
	 * Statistical evaluation of the number of generations of the
	 * SimulationDatasets
	 */
	private Evaluation generationEvaluation;

	/*
	 * Correlation between the cooperation value and the average payoff of the
	 * SimulationDatasets
	 * 
	 * @Transient private Correlation payoffCorrelation;
	 */

	/**
	 * The network on which the simulation was performed.
	 */
	private CustomGraph graph;

	///// Getter /////

	/**
	 * Returns the name or the id if no name is set.
	 *
	 * @return the name
	 */
	public String getName() {
		if (name == null || name == "")
			return String.valueOf(getKey());
		return name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	//@JsonIgnore
	public String getUserId() {
		return this.userId;
	}

	//@JsonProperty
	public Evaluation getCooperationEvaluation() {
		return cooperationEvaluation;
	}

	//@JsonProperty
	public Evaluation getPayoffEvaluation() {
		return payoffEvaluation;
	}

	//@JsonProperty
	public Evaluation getGenerationEvaluation() {
		return generationEvaluation;
	}

	/*
	 * @JsonProperty public Correlation getPayoffCorrelation() { return
	 * payoffCorrelation; }
	 */
	@JsonIgnore
	public CustomGraph getNetwork() {
		return this.graph;
	}

	public double averageCooperationValue() {
		if (getCooperationEvaluation() == null)
			return this.cooperativiaty;
		return getCooperationEvaluation().getAverage();
	}

	public double averagePayoffValue() {
		if (getPayoffEvaluation() == null)
			return this.wealth;
		return getPayoffEvaluation().getAverage();
	}

	public double getCooperativiaty() {
		return cooperativiaty;
	}

	public void setCooperativiaty(double cooperativiaty) {
		this.cooperativiaty = cooperativiaty;
	}

	public double getWealth() {
		return wealth;
	}

	public void setWealth(double wealth) {
		this.wealth = wealth;
	}

	public String getGraphKey() {
		if (this.graph != null) {
			return this.graph.getKey();
		}else{
			return null;
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setNetwork(CustomGraph graph) {
		this.graph = graph;
	}

	public void setCooperationEvaluation(Evaluation cooperationEvaluation) {
		if(cooperationEvaluation != null) {
			this.cooperationEvaluation = cooperationEvaluation;
			this.cooperativiaty = this.cooperationEvaluation.getAverage();
		}
	}

	public void setPayoffEvaluation(Evaluation payoffEvaluation) {

		if(payoffEvaluation != null) {  //TODO: this was == instead of !=, probably bug? but how come code worked?
			this.payoffEvaluation = payoffEvaluation;
			this.wealth = this.payoffEvaluation.getAverage();
		}
	}

	public void setGenerationEvaluation(Evaluation generationEvaluation) {
		if (generationEvaluation != null) {
			this.generationEvaluation = generationEvaluation;
		}
	}

	/*
	 * @JsonSetter public void setPayoffCorrelation(Correlation payoffCorrelation) {
	 * this.payoffCorrelation = payoffCorrelation; }
	 */

	///// Methods /////

	abstract public void evaluate();

	public boolean isEvaluated() {

		if (getCooperationEvaluation() == null)
			return false;
		if (getPayoffEvaluation() == null)
			return false;
		return true;
	}

	///// Print /////

	public TableRow toTableLine() {
		// TODO Auto-generated method stub
		return null;
	}

	public Table toTable() {
		// TODO Auto-generated method stub
		return null;
	}
}
