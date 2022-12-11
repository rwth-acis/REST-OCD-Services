package i5.las2peer.services.ocd.cooperation.data.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Objects of this class contain the meta data of simulation series group. They are
 * used to be send as JSON data to the client.
 *
 */
public class SimulationSeriesGroupMetaData implements Serializable {

	private static final long serialVersionUID = 1L;


	@JsonProperty
	private String key;

	@JsonProperty
	private String name;

	@JsonProperty
	private int size;

	@JsonProperty
	private Evaluation evaluation;

	public SimulationSeriesGroupMetaData() {

	}

	public SimulationSeriesGroupMetaData(SimulationSeriesGroup simulation) {

		this.key = simulation.getKey();
		this.name = simulation.getName();
		this.size = simulation.size();
		this.evaluation = simulation.getCooperationEvaluation();
		
	}

	@JsonProperty
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	@JsonProperty
	public int getSize() {
		return size;
	}

	@JsonProperty
	public Evaluation getEvaluation() {
		return evaluation;
	}


	public void setName(String name) {
		this.name = name;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

	@Override
	public String toString() {
		return "SimulationSeriesGroupMetaData{" +
				"key='" + key + '\'' +
				", name='" + name + '\'' +
				", size=" + size +
				", evaluation=" + evaluation +
				'}';
	}
}
