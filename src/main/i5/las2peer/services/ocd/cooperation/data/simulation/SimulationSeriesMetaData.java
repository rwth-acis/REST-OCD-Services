package i5.las2peer.services.ocd.cooperation.data.simulation;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * Objects of this class contain the meta data of simulation series. They are
 * used to be send as JSON data to the client.
 *
 */
public class SimulationSeriesMetaData implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private String name;
	private String graphName;
	private long graphId;
	private SimulationSeriesParameters parameters;
	private Evaluation evaluation;

	public SimulationSeriesMetaData() {

	}

	public SimulationSeriesMetaData(SimulationSeries series) {

		this.id = series.getId();
		this.name = series.getName();
		this.parameters = series.getParameters();
		this.evaluation = series.getCooperationEvaluation();		
		this.graphId = series.getParameters().getGraphId();
	}

	////// Getter //////

	@JsonProperty
	public long getId() {
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
	public long getGraphId() {
		return graphId;
	}

	@JsonProperty
	public String getGraphName() {
		return graphName;
	}

	////// Setter //////

	@JsonSetter
	public void setId(long id) {
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
	public void setGraphId(long graphId) {
		this.graphId = graphId;
	}

	@JsonSetter
	public void setGraphName(String graphName) {
		this.graphName = graphName;
	}

}
