package i5.las2peer.services.ocd.cooperation.data.simulation;

import java.util.ArrayList;
import java.util.List;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import i5.las2peer.services.ocd.cooperation.data.table.Table;
import i5.las2peer.services.ocd.cooperation.data.table.TableRow;
import i5.las2peer.services.ocd.graphs.Community;
import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * A SimulationSeriesGroup serves as a container for multiple SimulationSeries.
 *
 * Sometimes you want to do multiple simulations with a scaling parameter. This
 * class is meant to group the resulting SimulatonSeries together.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulationSeriesGroup extends SimulationAbstract {

	//ArangoDB
	public static final String collectionName = "simulationseriesgroup";
	public static final String simulationSeriesKeysColumnName = "SIMULATION_SERIES_KEYS";
	private static final String groupParametersColumnName = "GROUP_PARAMETERS";
	private static final String groupMetaDataColumnName = "GROUP_METADATA";

	////////// Entity Fields //////////

	@JsonIgnore
	private List<SimulationSeries> seriesList;

	@JsonProperty
	private List<String> simulationSeriesKeys;

	@JsonProperty
	private GroupParameters groupParameters;

	@JsonProperty
	private SimulationSeriesGroupMetaData groupMetaData;

	////////// Constructor //////////

	/**
	 * Creates a empty instance, that is used for persistence and testing purposes.
	 */
	public SimulationSeriesGroup() {
		this.seriesList = new ArrayList<>();
	}

	public SimulationSeriesGroup(List<SimulationSeries> list) {
		this.seriesList = list;
		if (this.simulationSeriesKeys == null){
			this.simulationSeriesKeys = new ArrayList<String>();
		}
		for (SimulationSeries simulationSeries : list){
			this.simulationSeriesKeys.add(simulationSeries.getKey());
		}
		// calculate and set the groupMetaData which is used for WebClient requests
		SimulationSeriesGroupMetaData groupMetaData = this.calculateMetaData();
		this.setGroupMetaData(groupMetaData);
	}



	public void setSeriesList(List<SimulationSeries> seriesList) {
		this.seriesList = seriesList;
	}

	@JsonIgnore
	public List<SimulationSeries> getSeriesList() {
		return this.seriesList;
	}

	/**
	 * @return the network ids used in the simulation series
	 */
	@JsonIgnore
	public List<String> getNetworkKeys() {

		List<String> networkIds = new ArrayList<String>();
		for (SimulationSeries series : seriesList) {
			networkIds.add(series.getSimulationSeriesParameters().getGraphKey());
		}
		return networkIds;
	}

	@Override
	public CustomGraph getNetwork() {
		if(this.getSeriesList() != null && this.getSeriesList().size() > 0) {
			return this.getSeriesList().get(0).getNetwork();
		}
		return null;
	}

	/**
	 * Creates the metaData object of this SimulationSeriesGroup. The metaData
	 * object is used to be sent to the web client.
	 *
	 * @return MetaData
	 */
	@JsonIgnore
	public SimulationSeriesGroupMetaData calculateMetaData() {
		if(this.getCooperationEvaluation() == null) {
			this.evaluate();
		}
		SimulationSeriesGroupMetaData metaData = new SimulationSeriesGroupMetaData(this);
		this.setGroupMetaData(metaData);
		return metaData;
	}

	public void setGroupMetaData(SimulationSeriesGroupMetaData groupMetaData) {
		this.groupMetaData = groupMetaData;
	}

	public SimulationSeriesGroupMetaData getGroupMetaData() {
		return groupMetaData;
	}

	/**
	 *
	 * @return SimulationSeries MetaData
	 */
	@JsonProperty
	public List<SimulationSeriesMetaData> getSeriesMetaData() {
		List<SimulationSeriesMetaData> list = new ArrayList<>(this.size());
		for (SimulationSeries sim : this.getSeriesList()) {
			list.add(sim.getMetaData());
		}
		return list;
	}

	public GroupParameters getGroupParameters() {
		return groupParameters;
	}

	public void setGroupParameters(GroupParameters groupParameters) {
		this.groupParameters = groupParameters;
	}

	public List<String> getSimulationSeriesKeys() {
		return simulationSeriesKeys;
	}

	public void setSimulationSeriesKeys(List<String> simulationSeriesKeys) {
		this.simulationSeriesKeys = simulationSeriesKeys;
	}


	/**
	 * Adds a SimulationSeries to this group
	 *
	 * @param series
	 *            SimulationSeries
	 */
	public void add(SimulationSeries series) {
		this.seriesList.add(series);
	}

	public void setSimulationSeries(List<SimulationSeries> seriesList) {
		this.seriesList = seriesList;
	}

	/////////////////// Methods ///////////////////////

	@Override
	public void evaluate() {

		setCooperationEvaluation(new Evaluation(getAverageFinalCooperationValues()));
		setPayoffEvaluation(new Evaluation(getAverageFinalPayoffValues()));
		//setGenerationEvaluation(new Evaluation(getAverageIterations()));

	}

	/**
	 * @return the number of contained SimulationSeries
	 */
	public int size() {
		return seriesList.size();
	}

	public int generations() {
		int maxSize = 0;
		for (SimulationSeries simulation : seriesList) {
			if (simulation.generations() > maxSize)
				maxSize = simulation.generations();
		}
		return maxSize;
	}

	//////////// Communities /////////////

	/**
	 * Return the average community cooperation values of all SimulationSeries
	 *
	 * @param communityList
	 *            the list of communities
	 * @return the values
	 */
	public double[] getAverageCommunityCooperationValues(List<Community> communityList) {
		int datasetCount = size();

		if (datasetCount < 1)
			throw new IllegalStateException("this simulation series group is empty");

		int communityCount = communityList.size();
		double[] averageValues = new double[communityCount];

		for (int communityId = 0; communityId < communityCount; communityId++) {
			averageValues[communityId] = getAverageCommunityCooperationValue(communityList.get(communityId));
		}
		return averageValues;
	}

	/**
	 * Returns the average community cooperation value of all SimulationSeries
	 *
	 * @param community
	 *            the Community
	 * @return average cooperation value
	 */
	public double getAverageCommunityCooperationValue(Community community) {
		int datasetCount = size();

		if (datasetCount < 1)
			throw new IllegalStateException("this simulation series group is empty");

		if (community.getMemberIndices() == null)
			throw new IllegalArgumentException("community has no memberlist");

		double total = 0.0;
		for (int datasetId = 0; datasetId < datasetCount; datasetId++) {
			total += getSeriesList().get(datasetId).getAverageCommunityCooperationValue(community);
		}

		return total / datasetCount;
	}

	///// final

	@JsonIgnore
	public double[] getAverageFinalCooperationValues() {
		int size = seriesList.size();
		double[] values = new double[size];
		for (int i = 0; i < size; i++) {
			values[i] = seriesList.get(i).averageCooperationValue();
		}
		return values;
	}

	@JsonIgnore
	public double[] getAverageFinalPayoffValues() {

		int size = seriesList.size();
		double[] values = new double[size];
		for (int i = 0; i < size; i++) {
			values[i] = seriesList.get(i).averagePayoffValue();
		}
		return values;
	}

	///// average

	@JsonIgnore
	public double[] getAverageIterations() {

		int size = seriesList.size();
		double[] values = new double[size];
		for (int i = 0; i < size; i++) {
			values[i] = seriesList.get(i).getGenerationEvaluation().getAverage();
		}
		return values;
	}


	public boolean equals(double d, double e) {
		if((d - e) < 0.0001) {
			return true;
		}
		return false;
	}

	////////// Print //////////

	@Override
	public Table toTable() {

		List<SimulationSeries> simulations = getSeriesList();
		Table table = new Table();

		// headline
		TableRow headline = new TableRow();
		headline.add("data").add(simulations.get(0).toHeadLine());
		table.add(headline);

		// average
		TableRow averageLine = new TableRow();
		averageLine.add("average").add(toTableLine());
		table.add(averageLine);

		// series
		for (int i = 0; i < simulations.size(); i++) {
			SimulationSeries series = simulations.get(i);
			table.add(series.toTableLine().addFront(i + 1));
		}
		return table;
	}

	@Override
	public TableRow toTableLine() {

		Evaluation coopEvaluation = getCooperationEvaluation();
		Evaluation payoffEvaluation = getPayoffEvaluation();
		//Evaluation generationEvaluation = getGenerationEvaluation();

		TableRow line = new TableRow();
		line.add(" ").add(" ").add(" ").add(" ").add(" ").add(" ");
		line.add(coopEvaluation.toTableLine());
		line.add(payoffEvaluation.toTableLine());
		//line.add(generationEvaluation.toTableLine());
		return line;
	}



	/**
	 * Update column values to be stored in the database.
	 * @param bd       Document holding updated values.
	 * @return         Document with updated values.
	 */
	public BaseDocument updateDocument(BaseDocument bd){
		bd.addAttribute(userIdColumnName, this.getUserId());
		ArrayList<String> simulationSeriesKeys = new ArrayList<String>();
		for (SimulationSeries simulationSeries : this.getSeriesList()){
			simulationSeriesKeys.add(simulationSeries.getKey());
		}
		bd.addAttribute(simulationSeriesKeysColumnName, simulationSeriesKeys);
		bd.addAttribute(groupParametersColumnName, this.getGroupParameters());
		bd.addAttribute(groupMetaDataColumnName, this.getGroupMetaData()); //TODO:DELETE
		// fields from superclass
		bd.addAttribute(super.nameColumnName, super.getName());
		bd.addAttribute(super.cooperativiatyColumnName, super.getCooperativiaty());
//		bd.addAttribute(super.wealthColumnName, super.getWealth());
		bd.addAttribute(super.cooperationEvaluationColumnName, new Evaluation(getAverageFinalCooperationValues()));
		bd.addAttribute(super.payoffEvaluationColumnName, new Evaluation(getAverageFinalPayoffValues()));

		// extra attribute for the WebClient
		if(this.getSimulationSeriesKeys() != null) {
			bd.addAttribute("size", this.getSimulationSeriesKeys().size());
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
	 * returned in a query into an array list
	 * @param listToParse         Object to parse as a list
	 * @return                    ArrayList representation of the input object
	 */
	public static ArrayList<String> documentToArrayList(Object listToParse){
		ObjectMapper objectMapper = new ObjectMapper();
		ArrayList<String> res;
		res = objectMapper.convertValue(listToParse, ArrayList.class);
		return res;
	}

	public static SimulationSeriesGroup load(String key, ArangoDatabase db, String transId) {
		ObjectMapper objectMapper = new ObjectMapper();
		SimulationSeriesGroup simulationSeriesGroup = new SimulationSeriesGroup();
		ArangoCollection collection = db.collection(collectionName);

		BaseDocument bd = collection.getDocument(key, BaseDocument.class);
		if (bd != null) {
			simulationSeriesGroup.setKey(bd.getKey());
			simulationSeriesGroup.setSimulationSeriesKeys(objectMapper.convertValue(bd.getAttribute(simulationSeriesKeysColumnName), ArrayList.class));
			simulationSeriesGroup.setGroupParameters(objectMapper.convertValue(bd.getAttribute(groupParametersColumnName), GroupParameters.class));

			// fields from superclass
			simulationSeriesGroup.setName((String) bd.getAttribute(nameColumnName));
			simulationSeriesGroup.setCooperativiaty((double) bd.getAttribute(cooperativiatyColumnName));
//			simulationSeriesGroup.setWealth((double) bd.getAttribute(wealthColumnName));
			simulationSeriesGroup.setCooperationEvaluation(objectMapper.convertValue(bd.getAttribute(cooperationEvaluationColumnName), Evaluation.class));
			simulationSeriesGroup.setPayoffEvaluation(objectMapper.convertValue(bd.getAttribute(payoffEvaluationColumnName), Evaluation.class));
//			simulationSeriesGroup.setGenerationEvaluation(objectMapper.convertValue(bd.getAttribute(generationEvaluationColumnName), Evaluation.class));


			// set metadata related to the simulation series group
			simulationSeriesGroup.setGroupMetaData(objectMapper.convertValue(bd.getAttribute(groupMetaDataColumnName), SimulationSeriesGroupMetaData.class));
		}
		else {
			System.out.println("SimulationSeriesGroup with key " + key + " not found.");
		}
		return simulationSeriesGroup;
	}



}
