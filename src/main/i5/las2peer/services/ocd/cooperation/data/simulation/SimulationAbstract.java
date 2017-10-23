package i5.las2peer.services.ocd.cooperation.data.simulation;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

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
@MappedSuperclass
public abstract class SimulationAbstract implements TableInterface {

	/**
	 * The id is used as persistence primary key
	 */
	@Id
	@GeneratedValue
	private long Id;

	/**
	 * The name of the simulation
	 */
	@Basic
	private String name;

	/**
	 * The Id of the owning user
	 */
	@Basic
	private long userId;

	/**
	 * cooperativity the simulation
	 */
	@Basic
	private double cooperativiaty;

	/**
	 * Wealth the simulation
	 */
	@Basic
	private double wealth;

	/**
	 * Statistical evaluation of the cooperation values of the
	 * SimulationDatasets
	 */
	@Embedded
	private Evaluation cooperationEvaluation;

	/**
	 * Statistical evaluation of the payoff values of the SimulationDatasets
	 */
	@AttributeOverrides({
		    @AttributeOverride(name="average",column=@Column(name="payoffAverage")),
			@AttributeOverride(name = "variance", column = @Column(name = "payoffvariance")),
			@AttributeOverride(name = "deviation", column = @Column(name = "payoffdeviation")),
			@AttributeOverride(name = "maximum", column = @Column(name = "payoffmaximum")),
			@AttributeOverride(name = "minimum", column = @Column(name = "payoffminimum")),
		  })
	@Embedded
	private Evaluation payoffEvaluation;

	/**
	 * Statistical evaluation of the number of generations of the
	 * SimulationDatasets
	 */
	@Transient
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
	@ManyToOne(cascade = CascadeType.ALL, targetEntity = CustomGraph.class, fetch=FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "graphId", referencedColumnName = CustomGraph.idColumnName, insertable = false, updatable = false),
			@JoinColumn(name = "username", referencedColumnName = CustomGraph.userColumnName, insertable = false, updatable = false) })
	private CustomGraph graph = new CustomGraph();

	///// Getter /////

	/**
	 * Returns a unique id.
	 * 
	 * @return the persistence id
	 */
	@JsonIgnore
	public long getId() {
		return this.Id;
	}

	/**
	 * Returns the name or the id if no name is set.
	 * 
	 * @return the name
	 */
	@Override
	@JsonProperty
	public String getName() {
		if (name == null || name == "")
			return String.valueOf(getId());
		return name;
	}

	@JsonIgnore
	public long getUserId() {
		return this.userId;
	}

	@JsonProperty
	public Evaluation getCooperationEvaluation() {
		return cooperationEvaluation;
	}

	@JsonProperty
	public Evaluation getPayoffEvaluation() {
		return payoffEvaluation;
	}

	@JsonProperty
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

	///// Setter /////

	public void setName(String name) {
		this.name = name;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	@JsonSetter
	public void setNetwork(CustomGraph graph) {
		this.graph = graph;
	}

	@JsonSetter
	public void setCooperationEvaluation(Evaluation cooperationEvaluation) {
		this.cooperationEvaluation = cooperationEvaluation;
		this.cooperativiaty = cooperationEvaluation.getAverage();
	}

	@JsonSetter
	public void setPayoffEvaluation(Evaluation payoffEvaluation) {
		this.payoffEvaluation = payoffEvaluation;
		this.wealth = cooperationEvaluation.getAverage();
	}

	@JsonSetter
	public void setGenerationEvaluation(Evaluation generationEvaluation) {
		this.generationEvaluation = generationEvaluation;
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

	@Override
	public Table toTable() {
		// TODO Auto-generated method stub
		return null;
	}

}
