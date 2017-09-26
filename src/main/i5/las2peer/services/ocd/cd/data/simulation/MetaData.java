package i5.las2peer.services.ocd.cd.data.simulation;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class MetaData implements Serializable {

	private static final long serialVersionUID = 1L;

	long seriesId;

	private Parameters parameters;
	private Evaluation evaluation;

	public MetaData() {

	}

	public MetaData(SimulationSeries series) {

		this.seriesId = series.getId();
		this.parameters = series.getParameters();
		this.evaluation = series.getCooperationEvaluation();
	}

	////// Getter //////

	@JsonProperty
	public long getSeriesId() {
		return seriesId;
	}

	@JsonProperty
	public Parameters getParameters() {
		return parameters;
	}

	@JsonProperty
	public Evaluation getEvaluation() {
		return evaluation;
	}

	////// Setter //////
	@JsonSetter
	public void setSeriesId(long seriesId) {
		this.seriesId = seriesId;
	}

	@JsonSetter
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}

	@JsonSetter
	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

}
