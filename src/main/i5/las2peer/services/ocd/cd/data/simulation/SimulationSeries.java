package i5.las2peer.services.ocd.cd.data.simulation;

import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import i5.las2peer.services.ocd.ServiceClass;
import i5.las2peer.services.ocd.cd.data.SimulationEntityHandler;
import i5.las2peer.services.ocd.cd.data.mapping.Correlation;
import i5.las2peer.services.ocd.cd.data.table.Table;
import i5.las2peer.services.ocd.cd.data.table.TableRow;
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
@Entity
public class SimulationSeries extends SimulationAbstract {

	/////////////// Entity Fields ///////////////

	/**
	 * The id of the owner
	 */
	@Basic
	private long userId;

	/**
	 * The simulation parameters used for this simulation series
	 */
	@Embedded
	private SimulationSeriesParameters parameters;

	/**
	 * Statistical evaluation of the number of generations of the
	 * SimulationDatasets
	 */
	@Transient
	private Evaluation generationEvaluation;

	/**
	 * Correlation between the cooperation value and the average payoff of the
	 * SimulationDatasets
	 */
	@Transient
	private Correlation payoffCorrelation;

	/**
	 * A simulations series consists of multiple simulation datasets
	 */
	@OneToMany(mappedBy="simulationSeries", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private List<SimulationDataset> simulationDatasets;

	/**
	 * A simulation series can be part of multiple groups
	 */
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<SimulationSeriesGroup> simulationGroups;

	/**
	 * Highest number of iterations of a dataset
	 */
	@Basic
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
	 * @param parameters
	 * @param simulationDatasets
	 */
	public SimulationSeries(SimulationSeriesParameters parameters, List<SimulationDataset> datasets, CustomGraph graph) {
		this.setParameters(parameters);
		this.setSimulationDatasets(datasets);
		this.setNetwork(graph);
	}
	
	/**
	 * Creates a instance with datasets and parameters
	 * 
	 * @param parameters
	 * @param simulationDatasets
	 */
	public SimulationSeries(SimulationSeriesParameters parameters, List<SimulationDataset> datasets) {
		this.setParameters(parameters);
		this.setSimulationDatasets(datasets);
	}

	////////////////// Getter /////////////////////



	@Override
	@JsonIgnore
	public long getUserId() {
		return userId;
	}

	@JsonIgnore
	public long getGenerations() {
		return generations;
	}

	@JsonProperty
	public SimulationSeriesParameters getParameters() {
		return parameters;
	}

	@Override
	@JsonProperty
	public Evaluation getGenerationEvaluation() {
		return generationEvaluation;
	}

	@Override
	@JsonProperty
	public Correlation getPayoffCorrelation() {
		return payoffCorrelation;
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

	@JsonIgnore
	public List<SimulationSeriesGroup> getSimulationSeriesGroups() {
		return this.simulationGroups;
	}
	
	////////////////// Setter /////////////////////

	@Override
	@JsonSetter
	public void setUserId(long userId) {
		this.userId = userId;
	}

	@JsonSetter
	public void setGenerations(int generations) {
		this.generations = generations;
	}

	@JsonSetter
	public void setParameters(SimulationSeriesParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	@JsonSetter
	public void setGenerationEvaluation(Evaluation generationEvaluation) {
		this.generationEvaluation = generationEvaluation;
	}

	@Override
	@JsonSetter
	public void setPayoffCorrelation(Correlation payoffCorrelation) {
		this.payoffCorrelation = payoffCorrelation;
	}

	@JsonSetter
	public void setSimulationDatasets(List<SimulationDataset> simulationDatasets) {
		this.simulationDatasets = simulationDatasets;
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
	 * @param list
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

		SimulationSeriesParameters parameters = getParameters();
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

		SimulationSeriesParameters parameters = getParameters();
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

}
