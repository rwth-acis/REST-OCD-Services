package i5.las2peer.services.ocd.cooperation.data.simulation;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Objects of this class contain the meta data of simulation series. They are
 * used to be send as JSON data to the client.
 *
 */
public class SimulationSeriesMetaData implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String name;
	private String graphName;
	private String graphId;
	private SimulationSeriesParameters parameters;
	private Evaluation evaluation;

	public SimulationSeriesMetaData() {

	}

	public SimulationSeriesMetaData(SimulationSeries series) {

		this.id = series.getKey();
		this.name = series.getName();
		this.parameters = series.getSimulationSeriesParameters();
		this.evaluation = series.getCooperationEvaluation();		
		this.graphId = series.getSimulationSeriesParameters().getGraphKey();
		this.graphName = series.getSimulationSeriesParameters().getGraphName();

	}

	////// Getter //////

	@JsonProperty
	public String getId() {
		return id;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	@JsonProperty
	public SimulationSeriesParameters getParameters() {
		return parameters;
	}

	@JsonProperty
	public Evaluation getEvaluation() {
		return evaluation;
	}

	@JsonProperty
	public String getGraphId() {
		return graphId;
	}

	@JsonProperty
	public String getGraphName() {
		return graphName;
	}

	////// Setter //////

	@JsonSetter
	public void setId(String id) {
		this.id = id;
	}

	@JsonSetter
	public void setName(String name) {
		this.name = name;
	}

	@JsonSetter
	public void setParameters(SimulationSeriesParameters parameters) {
		this.parameters = parameters;
	}

	@JsonSetter
	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

	@JsonSetter
	public void setGraphId(String graphId) {
		this.graphId = graphId;
	}

	@JsonSetter
	public void setGraphName(String graphName) {
		this.graphName = graphName;
	}

}
