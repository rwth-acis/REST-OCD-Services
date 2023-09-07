package i5.las2peer.services.ocd.cooperation.data.simulation;

import java.beans.ConstructorProperties;
import java.io.Serializable;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulationSeriesParameters implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty
	private String graphKey;

	@JsonProperty
	private GameType game;


	@JsonProperty
	private double payoffCC;

	@JsonProperty
	private double payoffCD;

	@JsonProperty
	private double payoffDD;


	@JsonProperty
	private double payoffDC;
		
	/**
	* the payoff cost value. Used only with a cost variant game. 
	*/
	private double cost; 
	
	/**
	* the payoff benefit value. Used only with a cost variant game. 
	*/
	private double benefit;

	@JsonProperty
	private DynamicType dynamic;

	@JsonProperty
	private double dynamicValue;

	/**
	 * the break condition for the simulaiton
	 */
	@JsonProperty
	private ConditionType condition;


	/**
	* the maximum rounds of a Simulation
	*/
	@JsonProperty
	private int maxIterations;
	
	/**
	* the minimum rounds of a Simulation
	*/
	@JsonProperty
	private int minIterations;
	
	/**
	* time window for the break condition
	*/
	@JsonProperty
	private int timeWindow;
	
	/**
	* time window for the break condition
	*/
	@JsonProperty
	private int threshold;

	/**
	 * how often a {@link Simulation} is executed resulting in multiple
	 * {@link SimulationDataset}s as part of one {@link SimulationSeries}
	 */
	@JsonProperty
	private int iterations;

	@JsonProperty
	private String simulationName;

	@JsonProperty
	private String graphName;

	////////// Constructor //////////

	public SimulationSeriesParameters() {

	}


	@ConstructorProperties({"name", "iterations","payoffDC","payoffCC","payoffDD","payoffCD",
			"dynamic","dynamicValue","condition","maxIterations","graphId"})
	public SimulationSeriesParameters( String simulationName, int iterations, double payoffDC, double payoffCC, double payoffDD,
									  double payoffCD, String dynamic, double dynamicValue, String condition,
									  int maxIterations, String graphKey) {
		this.graphKey = graphKey;
		this.payoffCC = payoffCC;
		this.payoffCD = payoffCD;
		this.payoffDD = payoffDD;
		this.payoffDC = payoffDC;
		this.setDynamic(dynamic);
		this.dynamicValue = dynamicValue;
		this.setCondition(condition);
		this.maxIterations = maxIterations;
		this.iterations = iterations;
		this.simulationName = simulationName;
	}


	////////// Getter //////////



	public String getGraphKey() {
		return graphKey;
	}


	public DynamicType getDynamic() {
		if(dynamic == null)
			return DynamicType.UNKNOWN;
		return dynamic;
	}


	public int getIterations() {
		return iterations;
	}


	public GameType getGame() {
		if (game == null) {
			this.game = GameType.getGameType(payoffCC, payoffCD, payoffDC, payoffDD);
		}
		return this.game;
	}


	public double getPayoffCC() {
		return payoffCC;
	}

	public double getPayoffCD() {
		return payoffCD;
	}

	public double getPayoffDD() {
		return payoffDD;
	}

	public double getPayoffDC() {
		return payoffDC;
	}

	public double getCost() {
		return cost;
	}

	public double getBenefit() {
		return benefit;
	}

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
		return new int[] { maxIterations, minIterations, timeWindow };
	}

	////////// Setter //////////


	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public void setGraphKey(String graphKey) {
		this.graphKey = graphKey;
	}

	public void setDynamic(String dynamic) {
		this.dynamic = DynamicType.fromString(dynamic);
	}

	public void setGame(String game) {
		this.game = GameType.fromString(game);
	}

	public void setDynamic(DynamicType dynamic) {
		this.dynamic = dynamic;
	}

	public void setCondition(String condition) {
		this.condition = ConditionType.fromString(condition);
	}

	public void setGame(GameType game) {
		this.game = game;
	}

	public void setDynamicValue(double dynamicValue) {
		this.dynamicValue = dynamicValue;
	}

	public void setPayoffCC(double payoffCC) {
		this.payoffCC = payoffCC;
	}

	public void setPayoffCD(double payoffCD) {
		this.payoffCD = payoffCD;
	}

	public void setPayoffDD(double payoffDD) {
		this.payoffDD = payoffDD;
	}

	public void setPayoffDC(double payoffDC) {
		this.payoffDC = payoffDC;
	}

	public void setBenefit(double benefit) {
		this.benefit = benefit;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public void setCondition(ConditionType condition) {
		this.condition = condition;
	}

	public String getSimulationName() {
		return simulationName;
	}

	public void setSimulationName(String simulationName) {
		this.simulationName = simulationName;
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

	@Override
	public String toString() {
		return "SimulationSeriesParameters{" +
				"graphKey='" + graphKey + '\'' +
				", game=" + game +
				", payoffCC=" + payoffCC +
				", payoffCD=" + payoffCD +
				", payoffDD=" + payoffDD +
				", payoffDC=" + payoffDC +
				", cost=" + cost +
				", benefit=" + benefit +
				", dynamic=" + dynamic +
				", dynamicValue=" + dynamicValue +
				", condition=" + condition +
				", maxIterations=" + maxIterations +
				", minIterations=" + minIterations +
				", timeWindow=" + timeWindow +
				", threshold=" + threshold +
				", iterations=" + iterations +
				", simulationName='" + simulationName + '\'' +
				", graphName='" + graphName + '\'' +
				'}';
	}
}
