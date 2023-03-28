package i5.las2peer.services.ocd.cooperation.data.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import com.fasterxml.jackson.databind.ObjectMapper;
import i5.las2peer.services.ocd.cooperation.data.mapping.correlation.Correlation;
import i5.las2peer.services.ocd.cooperation.data.table.Table;
import i5.las2peer.services.ocd.cooperation.data.table.TableRow;
import i5.las2peer.services.ocd.graphs.Community;
import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * A SimulationSeries serves as a container for multiple SimulationDatasets.
 *
 * If you repeat the same Simulation multiple times, you will get multiple
 * SimulationDatasets with the same parameters. This class is meant to group
 * them together and allow statistical evaluation of the data sets.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class SimulationSeries extends SimulationAbstract {

	//ArangoDB
	public static final String collectionName = "simulationseries";
	public static final String simulationSeriesParametersColumnName = "SIMULATION_SERIES_PARAMETERS";
	public static final String simulationDatasetsColumnName = "SIMULATION_DATASETS";
	public static final String simulationSeriesGroupKeysColumnName = "SIMULATION_SERIES_GROUP_KEYS";
	public static final String generationsColumnName = "GENERATIONS";

	/////////////// Entity Fields ///////////////


	/**
	 * The simulation parameters used for this simulation series
	 */
	private SimulationSeriesParameters simulationSeriesParameters;


	/**
	 * Statistical evaluation of the number of generations of the
	 * SimulationDatasets
	 */
	private Evaluation generationEvaluation;

	/**
	 * Correlation between the cooperation value and the average payoff of the
	 * SimulationDatasets
	 */
	private Correlation payoffCorrelation;

	/**
	 * A simulations series consists of multiple simulation datasets
	 */
	private List<SimulationDataset> simulationDatasets;


	/**
	 * Highest number of iterations of a dataset
	 */
	private int generations;

	///////////////// Constructor //////////////////

	/**
	 * Creates a empty instance, that is used for persistence and testing
	 * purposes.
	 */
	public SimulationSeries() {

	}

	/**
	 * Creates a instance with datasets, parameters and graph.
	 *
	 * @param simulationSeriesParameters the parameters
	 * @param datasets the datasets
	 * @param graph the graph
	 */
	public SimulationSeries(SimulationSeriesParameters simulationSeriesParameters, List<SimulationDataset> datasets, CustomGraph graph) {
		this.setSimulationSeriesParameters(simulationSeriesParameters);
		this.setSimulationDatasets(datasets);
		this.setNetwork(graph);
	}

	/**
	 * Creates a instance with datasets and parameters
	 *
	 * @param simulationSeriesParameters the parameters
	 * @param datasets the datasets
	 */
	public SimulationSeries(SimulationSeriesParameters simulationSeriesParameters, List<SimulationDataset> datasets) {
		this.setSimulationSeriesParameters(simulationSeriesParameters);
		this.setSimulationDatasets(datasets);
	}



	@JsonIgnore
	public long getGenerations() {
		return generations;
	}

	@JsonProperty
	public SimulationSeriesParameters getSimulationSeriesParameters() {
		return simulationSeriesParameters;
	}

	@Override
	@JsonProperty
	public Evaluation getGenerationEvaluation() {
		return generationEvaluation;
	}

	@JsonProperty
	public List<SimulationDataset> getSimulationDatasets() {
		return simulationDatasets;
	}

	@JsonIgnore
	public SimulationSeriesMetaData getMetaData() {
		SimulationSeriesMetaData meta = new SimulationSeriesMetaData(this);
		return meta;
	}



	public void setGenerations(int generations) {
		this.generations = generations;
	}


	public void setSimulationSeriesParameters(SimulationSeriesParameters simulationSeriesParameters) {
		this.simulationSeriesParameters = simulationSeriesParameters;
	}

	@Override
	public void setGenerationEvaluation(Evaluation generationEvaluation) {
		this.generationEvaluation = generationEvaluation;
	}

	public void setSimulationDatasets(List<SimulationDataset> simulationDatasets) {
		this.simulationDatasets = simulationDatasets;
	}


	public Correlation getPayoffCorrelation() {
		return payoffCorrelation;
	}

	public void setPayoffCorrelation(Correlation payoffCorrelation) {
		this.payoffCorrelation = payoffCorrelation;
	}



	/////////////////// Methods ///////////////////////

	/**
	 * Create {@link Evaluation} objects with the values given by
	 * {@link #getFinalCooperationValues()} and {@link #getFinalPayoffValues()}.
	 *
	 */
	@Override
	public void evaluate() {
		this.setCooperationEvaluation(new Evaluation(getFinalCooperationValues()));
		this.setPayoffEvaluation(new Evaluation(getFinalPayoffValues()));
		// this.setGenerationEvaluation(new Evaluation(iterations()));
		// payoffCorrelation = new Correlation(getFinalCooperationValues(),
		// getFinalPayoffValues());
	}

	/**
	 * @return the number of SimulationDatasets
	 */
	public int size() {
		if (simulationDatasets == null)
			return 0;
		return simulationDatasets.size();
	}

	/**
	 * @return the number of generations of the longest SimulationDataset
	 */
	public int generations() {

		if (getSimulationDatasets() == null)
			return 0;

		int maxSize = 0;
		for (SimulationDataset dataset : simulationDatasets) {
			if (dataset.generations() > maxSize)
				maxSize = dataset.generations();
		}
		return maxSize;
	}

	public double[] iterations() {

		int size = size();
		double[] iterations = new double[size];
		for (int i = 0; i < size; i++) {
			iterations[i] = getSimulationDatasets().get(i).getIterations();
		}

		return iterations;
	}

	public void normalize() {
		int maxSize = generations();
		for (SimulationDataset dataset : simulationDatasets) {
			dataset.fill(maxSize);
		}
	}

	@Override
	public double averageCooperationValue() {
		if (getCooperationEvaluation() == null)
			setCooperationEvaluation(new Evaluation(getFinalCooperationValues()));
		return getCooperationEvaluation().getAverage();
	}

	@Override
	public double averagePayoffValue() {
		if (getPayoffEvaluation() == null)
			setPayoffEvaluation(new Evaluation(getFinalPayoffValues()));
		return getPayoffEvaluation().getAverage();
	}

	/**
	 * Return the average community cooperation values of all SimulationDatasets
	 * *
	 *
	 * @param communityList
	 *            the list of communities
	 * @return the values
	 */
	public double[] getAverageCommunityCooperationValues(List<Community> communityList) {
		int datasetCount = size();

		if (datasetCount < 1)
			throw new IllegalStateException("this simulation series is empty");

		int communityCount = communityList.size();
		double[] averageValues = new double[communityCount];

		for (int communityId = 0; communityId < communityCount; communityId++) {
			averageValues[communityId] = getAverageCommunityCooperationValue(communityList.get(communityId));
		}
		return averageValues;
	}

	/**
	 * Returns the average community cooperation value of the SimulationDatasets
	 * *
	 *
	 * @param community
	 *            the Community
	 * @return average cooperation value
	 */
	public double getAverageCommunityCooperationValue(Community community) {
		int datasetCount = size();

		if (datasetCount < 1)
			throw new IllegalStateException("this simulation series is empty");

		if (community.getMemberIndices() == null)
			throw new IllegalArgumentException("community has no memberlist");

		double total = 0.0;
		for (int datasetId = 0; datasetId < datasetCount; datasetId++) {
			total += getSimulationDatasets().get(datasetId).getCommunityCooperationValue(community.getMemberIndices());
		}

		return total / datasetCount;
	}

	///// Final

	/**
	 * @return array of the cooperations values of the final state of all
	 *         SimulationDatasets
	 */
	@JsonIgnore
	public double[] getFinalCooperationValues() {

		int size = size();
		double[] values = new double[size];
		for (int i = 0; i < size; i++) {
			values[i] = simulationDatasets.get(i).getFinalCooperationValue();
		}
		return values;
	}

	/**
	 * @return array of the payoff values of the final state of all
	 *         SimulationDatasets
	 */
	@JsonIgnore
	public double[] getFinalPayoffValues() {

		int size = size();
		double[] values = new double[size];
		for (int i = 0; i < size; i++) {
			values[i] = simulationDatasets.get(i).getFinalPayoffValue();
		}
		return values;
	}

	///// over time

	@JsonIgnore
	public double[] getAverageCooperationValuesOverTime() {

		int size = generations();
		double[] values = new double[size];
		for (int i = 0; i < size; i++) {
			double total = 0;
			for (SimulationDataset dataset : simulationDatasets) {
				total += dataset.getCooperationValues().get(i);
			}
			values[i] = total / getSimulationDatasets().size();
		}
		return values;
	}

	@JsonIgnore
	public double[] getAveragePayoffValuesOverTime() {

		int size = generations();
		double[] values = new double[size];
		for (int i = 0; i < size; i++) {
			double total = 0;
			for (SimulationDataset dataset : simulationDatasets) {
				total += dataset.getPayoffValues().get(i);
			}
			values[i] = total / getSimulationDatasets().size();
		}
		return values;
	}

	////////////// Print Data /////////////

	@Override
	public Table toTable() {

		List<SimulationDataset> simulationDatasets = getSimulationDatasets();
		Table table = new Table();

		// headline
		TableRow headline = new TableRow();
		headline.add("data").add(simulationDatasets.get(0).toHeadLine());
		table.add(headline);

		// datasets
		for (int i = 0; i < simulationDatasets.size(); i++) {
			SimulationDataset data = simulationDatasets.get(i);
			table.add(data.toTableLine().addFront(i + 1));
		}
		return table;
	}

	@Override
	public TableRow toTableLine() {

		SimulationSeriesParameters parameters = getSimulationSeriesParameters();
		Evaluation coopEvaluation = getCooperationEvaluation();
		Evaluation payoffEvaluation = getPayoffEvaluation();
		// valuation generationEvaluation = getGenerationEvaluation();

		TableRow line = new TableRow();
		line.add(parameters.toTableLine());
		line.add(coopEvaluation.toTableLine());
		line.add(payoffEvaluation.toTableLine());
		// line.add(generationEvaluation.toTableLine());
		return line;
	}

	public TableRow toHeadLine() {

		SimulationSeriesParameters parameters = getSimulationSeriesParameters();
		Evaluation coopEvaluation = getCooperationEvaluation();
		Evaluation payoffEvaluation = getPayoffEvaluation();
		// Evaluation generationEvaluation = getGenerationEvaluation();

		TableRow line = new TableRow();
		line.add(parameters.toHeadLine());
		line.add(Evaluation.toHeadLine().suffix("#C"));
		line.add(Evaluation.toHeadLine().suffix("#P"));
		// line.add(generationEvaluation.toHeadLine().suffix("#G"));

		return line;

	}

	/**
	 * Update column values to be stored in the database.
	 * @param bd       Document holding updated values.
	 * @return         Document with updated values.
	 */
	public BaseDocument updateDocument(BaseDocument bd){
		bd.addAttribute(userIdColumnName, this.getUserId());
		bd.addAttribute(simulationSeriesParametersColumnName, this.getSimulationSeriesParameters());
		bd.addAttribute(generationsColumnName, this.getGenerations());
		bd.addAttribute(simulationDatasetsColumnName, this.getSimulationDatasets());
		// fields from superclass
		bd.addAttribute(nameColumnName, this.getName());
		bd.addAttribute(cooperativiatyColumnName, this.getCooperativiaty());
		bd.addAttribute(wealthColumnName, this.getWealth());
		bd.addAttribute(cooperationEvaluationColumnName, this.getCooperationEvaluation());
		bd.addAttribute(payoffEvaluationColumnName, this.getPayoffEvaluation());
		bd.addAttribute(generationEvaluationColumnName, this.getGenerationEvaluation());
		if(this.getNetwork() != null) {
			bd.addAttribute(graphKeyName, this.getNetwork().getKey());
		}


		return bd;
	}

	// Persistence Methods
	public void persist(ArangoDatabase db, String transId) {
		ArangoCollection collection = db.collection(collectionName);
		DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);
		BaseDocument bd = new BaseDocument();
		updateDocument(bd);
		collection.insertDocument(bd, createOptions);
		this.setKey(bd.getKey()); // if key is assigned before inserting (line above) the value is null
	}

	public void persist(ArangoDatabase db, String transId, String userId) {
		ArangoCollection collection = db.collection(collectionName);
		DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);
		BaseDocument bd = new BaseDocument();
		updateDocument(bd);
		bd.addAttribute(userIdColumnName, userId);
		collection.insertDocument(bd, createOptions);
		this.setKey(bd.getKey()); // if key is assigned before inserting (line above) the value is null
	}


	public void updateDB(ArangoDatabase db, String transId) {
		DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);

		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		updateDocument(bd);
		collection.updateDocument(this.getKey(), bd, updateOptions);
	}

	/**
	 * Helper method to convert object representation of a list
	 * returned in a query into a list of datasets
	 * @param simulationDatasetListObject         Object representing SimulationDataset list
	 * @return                                    ArrayList representation of the input object
	 */
	public static List<SimulationDataset> objectToSimulationDatasetList(Object simulationDatasetListObject){
		if (simulationDatasetListObject == null){
			return new ArrayList<SimulationDataset>(); // TODO: should this return null or empty list?
		}
		ObjectMapper objectMapper = new ObjectMapper();
		SimulationDataset[] simulationDatasetsArray = objectMapper.convertValue(simulationDatasetListObject, SimulationDataset[].class);
		List<SimulationDataset> simulationDatasetsList = Arrays.asList(simulationDatasetsArray);

		return simulationDatasetsList;
	}

	public static SimulationSeries load(String key, ArangoDatabase db, String transId) {
		ObjectMapper objectMapper = new ObjectMapper();
		SimulationSeries simulationSeries = new SimulationSeries();
		ArangoCollection collection = db.collection(collectionName);
		DocumentReadOptions readOpt = new DocumentReadOptions().streamTransactionId(transId);
		BaseDocument bd = collection.getDocument(key, BaseDocument.class, readOpt);
		if (bd != null) {
			simulationSeries.setKey(bd.getKey());
			simulationSeries.setUserId((String) bd.getAttribute(userIdColumnName));
			simulationSeries.setSimulationSeriesParameters(objectMapper.convertValue(bd.getAttribute(simulationSeriesParametersColumnName), SimulationSeriesParameters.class));
			simulationSeries.setGenerations((int) bd.getAttribute(generationsColumnName));
			simulationSeries.setSimulationDatasets(objectToSimulationDatasetList(bd.getAttribute(simulationDatasetsColumnName)));
			// fields from superclass
			simulationSeries.setName((String) bd.getAttribute(nameColumnName));
			simulationSeries.setCooperativiaty((double) bd.getAttribute(cooperativiatyColumnName));
			simulationSeries.setWealth((double) bd.getAttribute(wealthColumnName));
			simulationSeries.setCooperationEvaluation(objectMapper.convertValue(bd.getAttribute(cooperationEvaluationColumnName), Evaluation.class));
			simulationSeries.setPayoffEvaluation(objectMapper.convertValue(bd.getAttribute(payoffEvaluationColumnName), Evaluation.class));
			simulationSeries.setGenerationEvaluation(objectMapper.convertValue(bd.getAttribute(generationEvaluationColumnName), Evaluation.class));

			if(bd.getAttribute(graphKeyName) != null) {
				simulationSeries.setNetwork(CustomGraph.load((String) bd.getAttribute(graphKeyName), db, transId));
			}

		}
		else {
			System.out.println("SimulationSeries with key " + key + " not found.");
		}
		return simulationSeries;
	}

	@Override
	public String toString() {
		return "SimulationSeries{" +
				"key='" + this.getKey() + '\'' +
				", cooperationEvaluation='" + getCooperationEvaluation() + '\'' +
				", simulationSeriesParameters=" + simulationSeriesParameters +
				", generationEvaluation=" + generationEvaluation +
				", payoffCorrelation=" + payoffCorrelation +
				", simulationDatasets=" + simulationDatasets +
				", generations=" + generations +
				'}';
	}

}
