package i5.las2peer.services.ocd.cooperation.data.simulation;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import i5.las2peer.services.ocd.cooperation.data.table.TableRow;
import i5.las2peer.services.ocd.cooperation.simulation.Simulation;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.DynamicType;
import i5.las2peer.services.ocd.cooperation.simulation.game.GameType;
import i5.las2peer.services.ocd.cooperation.simulation.termination.ConditionType;

/**
 * Object of this class hold the parameters used for a simulation series.
 * They can be created by JSON parsed client requests. They can be stored as entity by JPA.
 * 
 */
@Embeddable
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulationSeriesParameters implements Serializable {

	private static final long serialVersionUID = 1L;

	////////// Entity Fields //////////

	@Basic
	private long graphId;
	
	@Enumerated(EnumType.STRING)
	private GameType game;

	@Basic
	private double payoffCC;

	@Basic
	private double payoffCD;

	@Basic
	private double payoffDD;

	@Basic
	private double payoffDC;
		
	/**
	* the payoff cost value. Used only with a cost variant game. 
	*/
	@Transient
	private double cost; 
	
	/**
	* the payoff benefit value. Used only with a cost variant game. 
	*/
	@Transient
	private double benefit;

	@Enumerated(EnumType.STRING)
	private DynamicType dynamic;

	@Basic
	private double dynamicValue;

	/**
	 * the break condition for the simulaiton
	 */
	@Enumerated(EnumType.STRING)
	private ConditionType condition;

	/**
	* the maximum rounds of a Simulation
	*/
	@Basic
	private int maxIterations;
	
	/**
	* the minimum rounds of a Simulation
	*/
	@Basic
	private int minIterations;
	
	/**
	* time window for the break condition
	*/
	@Basic
	private int timeWindow;
	
	/**
	* time window for the break condition
	*/
	@Basic
	private int threshold;

	/**
	 * how often a {@link Simulation} is executed resulting in multiple
	 * {@link SimulationDataset}s as part of one {@link SimulationSeries}
	 */
	@Basic
	private int iterations;

	@Basic
	private String simulationName;

	@Basic
	private String graphName;

	////////// Constructor //////////

	public SimulationSeriesParameters() {

	}

	public SimulationSeriesParameters(SimulationSeries series, long graphId, GameType game, double payoffCC, double payoffCD,
			double payoffDD, double payoffDC, DynamicType dynamic, double dynamicValue, int iterations) {

		this.setGraphId(graphId);
		this.payoffCC = payoffCC;
		this.payoffCD = payoffCD;
		this.payoffDC = payoffDC;
		this.payoffDD = payoffDD;
		this.setDynamic(dynamic);
		this.setDynamicValue(dynamicValue);
		this.setIterations(iterations);
	}

	////////// Getter //////////

	@JsonProperty
	public String getName() {
		return this.simulationName;
	}

	@JsonProperty
	public long getGraphId() {
		return graphId;
	}

	@JsonProperty
	public DynamicType getDynamic() {
		if(dynamic == null)
			return DynamicType.UNKNOWN;
		return dynamic;
	}

	@JsonProperty
	public int getIterations() {
		return iterations;
	}

	@JsonProperty
	public GameType getGame() {
		if (game == null) {
			this.game = GameType.getGameType(payoffCC, payoffCD, payoffDC, payoffDD);
		}
		return this.game;
	}

	@JsonProperty
	public double getPayoffCC() {
		return payoffCC;
	}

	@JsonProperty
	public double getPayoffCD() {
		return payoffCD;
	}

	@JsonProperty
	public double getPayoffDD() {
		return payoffDD;
	}

	@JsonProperty
	public double getPayoffDC() {
		return payoffDC;
	}

	@JsonProperty
	public double getCost() {
		return cost;
	}

	@JsonProperty
	public double getBenefit() {
		return benefit;
	}

	@JsonProperty
	public ConditionType getCondition() {
		if (condition == null)
			return ConditionType.UNKNOWN;
		return condition;
	}

	/**
	 * Return the payoff values as array
	 * @return payoff values
	 */
	@JsonIgnore
	public double[] getPayoffValues() {
		return new double[] { payoffCC, payoffCD, payoffDC, payoffDD };
	}

	/**
	 * Return the dynamic values as array
	 * 
	 * @return dynamic values
	 */
	@JsonIgnore
	public double[] getDynamicValues() {
		return new double[] { dynamicValue };
	}

	/**
	 * Return the condition values as array
	 * 
	 * @return condition values
	 */
	@JsonIgnore
	public int[] getConditionValues() {
		return new int[] { minIterations, maxIterations, timeWindow };
	}

	////////// Setter //////////

	@JsonSetter
	public void setName(String name) {
		this.simulationName = name;
	}

	@JsonSetter
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	@JsonSetter
	public void setGraphId(long graphId) {
		this.graphId = graphId;
	}

	@JsonSetter
	public void setDynamic(String dynamic) {
		this.dynamic = DynamicType.fromString(dynamic);
	}

	@JsonSetter
	public void setGame(String game) {
		this.game = GameType.fromString(game);
	}

	@JsonIgnore
	public void setDynamic(DynamicType dynamic) {
		this.dynamic = dynamic;
	}

	@JsonIgnore
	public void setGame(GameType game) {
		this.game = game;
	}

	@JsonSetter
	public void setDynamicValue(double dynamicValue) {
		this.dynamicValue = dynamicValue;
	}

	@JsonSetter
	public void setPayoffCC(double payoffCC) {
		this.payoffCC = payoffCC;
	}

	@JsonSetter
	public void setPayoffCD(double payoffCD) {
		this.payoffCD = payoffCD;
	}

	@JsonSetter
	public void setPayoffDD(double payoffDD) {
		this.payoffDD = payoffDD;
	}

	@JsonSetter
	public void setPayoffDC(double payoffDC) {
		this.payoffDC = payoffDC;
	}

	@JsonSetter
	public void setBenefit(double benefit) {
		this.benefit = benefit;
	}

	@JsonSetter
	public void setCost(double cost) {
		this.cost = cost;
	}

	///////////// Methods /////////////
	
	/**
	 * Normalize the payoff values
	 */
	public void normalize() {

		double total = Math.abs(payoffCC) + Math.abs(payoffCD) + Math.abs(payoffDC) + Math.abs(payoffDD);
		if (total != 0.0) {
			payoffCC = payoffCC / total;
			payoffCD = payoffCD / total;
			payoffDC = payoffDC / total;
			payoffDD = payoffDD / total;
		}

	}

	///////// Print /////////

	public TableRow toTableLine() {

		String format = "%.2f";
		TableRow line = new TableRow();
		line.add(getGame().shortcut()).add(String.format(format, getPayoffCC()))
				.add(String.format(format, getPayoffCD())).add(String.format(format, getPayoffDC()))
				.add(String.format(format, getPayoffDD())).add(getDynamic().shortcut());
		return line;
	}

	public TableRow toHeadLine() {

		TableRow line = new TableRow();
		line.add("Game").add("CC").add("CD").add("DC").add("DD").add("Dynamic");
		return line;
	}

	public String toFileName() {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getGame().shortcut()).append("_").append(getDynamic().shortcut());

		return stringBuilder.toString();

	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public int getMinIterations() {
		return minIterations;
	}

	public void setMinIterations(int minIterations) {
		this.minIterations = minIterations;
	}

	public int getTimeWindow() {
		return timeWindow;
	}

	public void setTimeWindow(int timeWindow) {
		this.timeWindow = timeWindow;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public double getDynamicValue() {
		return dynamicValue;
	}

	public String getGraphName() {
		return graphName;
	}

	public void setGraphName(String string) {
		this.graphName = string;
	}

}
