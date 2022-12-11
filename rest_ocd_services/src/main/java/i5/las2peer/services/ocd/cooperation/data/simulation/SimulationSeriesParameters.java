package i5.las2peer.services.ocd.cooperation.data.simulation;

import java.beans.ConstructorProperties;
import java.io.Serializable;

import javax.persistence.Embeddable;

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
@Embeddable
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulationSeriesParameters implements Serializable {

	private static final long serialVersionUID = 1L;

	//ArangoDB
	public static final String collectionName = "simulationseriesparameters";
	private static final String gameTypeColumnName = "GAME_TYPE";
	private static final String payoffCCColumnName = "PAYOFF_CC";
	private static final String payoffCDColumnName = "PAYOFF_CD";
	private static final String payoffDDColumnName = "PAYOFF_DD";
	private static final String payoffDCColumnName = "PAYOFF_DC";
	private static final String dynamicValueColumnName = "DYNAMIC_VALUE";
	private static final String dynamicTypeIdColumnName = "DYNAMIC_TYPE";
	private static final String conditionTypeIdColumnName = "CONDITION_TYPE";
	private static final String maxIterationsColumnName = "MAX_ITERATIONS";
	private static final String minIterationsColumnName = "MIN_ITERATIONS";
	private static final String timeWindowColumnName = "TIME_WINDOW";
	private static final String thresholdColumnName = "THRESHOLD";
	private static final String iterationsColumnName = "ITERATIONS";
	private static final String simulationNameColumnName = "SIMULATION_NAME";
	private static final String graphNameColumnName = "GRAPH_NAME";


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



	/**
	 * Update column values to be stored in the database.
	 * @param bd       Document holding updated values.
	 * @return         Document with updated values.
	 */
	public BaseDocument updateDocument(BaseDocument bd){
		bd.addAttribute(gameTypeColumnName, this.game.humanRead());
		bd.addAttribute(payoffCCColumnName, this.payoffCC);
		bd.addAttribute(payoffCDColumnName, this.payoffCD);
		bd.addAttribute(payoffDDColumnName, this.payoffDD);
		bd.addAttribute(payoffDCColumnName, this.payoffDC);
		bd.addAttribute(dynamicValueColumnName, this.dynamicValue);
		bd.addAttribute(dynamicTypeIdColumnName, this.dynamic.humanRead());
		bd.addAttribute(conditionTypeIdColumnName, this.condition.humanRead());
		bd.addAttribute(maxIterationsColumnName, this.maxIterations);
		bd.addAttribute(minIterationsColumnName, this.minIterations);
		bd.addAttribute(timeWindowColumnName, this.timeWindow);
		bd.addAttribute(thresholdColumnName, this.threshold);
		bd.addAttribute(iterationsColumnName, this.iterations);
		bd.addAttribute(simulationNameColumnName, this.simulationName);
		bd.addAttribute(graphNameColumnName, this.graphName);

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

	public static SimulationSeriesParameters load(String key, ArangoDatabase db, String transId) {
		SimulationSeriesParameters simulationSeriesParameters = new SimulationSeriesParameters();
		ArangoCollection collection = db.collection(collectionName);

		BaseDocument bd = collection.getDocument(key, BaseDocument.class);
		if (bd != null) {
			//simulationSeriesParameters.setKey(bd.getKey());
			simulationSeriesParameters.setGame(bd.getAttribute(gameTypeColumnName).toString());
			simulationSeriesParameters.setPayoffCC((double) bd.getAttribute(payoffCCColumnName));
			simulationSeriesParameters.setPayoffCD((double) bd.getAttribute(payoffCDColumnName));
			simulationSeriesParameters.setPayoffDD((double) bd.getAttribute(payoffDDColumnName));
			simulationSeriesParameters.setPayoffDC((double) bd.getAttribute(payoffDCColumnName));
			simulationSeriesParameters.setDynamicValue((double) bd.getAttribute(dynamicValueColumnName));
			simulationSeriesParameters.setDynamic(bd.getAttribute(dynamicTypeIdColumnName).toString());
			simulationSeriesParameters.setCondition(bd.getAttribute(conditionTypeIdColumnName).toString());
			simulationSeriesParameters.setMaxIterations((int) bd.getAttribute(maxIterationsColumnName));
			simulationSeriesParameters.setMinIterations((int) bd.getAttribute(minIterationsColumnName));
			simulationSeriesParameters.setTimeWindow((int) bd.getAttribute(timeWindowColumnName));
			simulationSeriesParameters.setThreshold((int) bd.getAttribute(thresholdColumnName));
			simulationSeriesParameters.setIterations((int) bd.getAttribute(iterationsColumnName));
			simulationSeriesParameters.setSimulationName(bd.getAttribute(simulationNameColumnName).toString());
			simulationSeriesParameters.setGraphName(bd.getAttribute(graphNameColumnName).toString());
		}
		else {
			System.out.println("SimulationSeriesParameter with key " + key + " not found.");
		}
		return simulationSeriesParameters;
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
