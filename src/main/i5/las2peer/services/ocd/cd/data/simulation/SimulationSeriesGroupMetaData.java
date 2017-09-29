package i5.las2peer.services.ocd.cd.data.simulation;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * Objects of this class contain the meta data of simulation series group. They are
 * used to be send as JSON data to the client.
 *
 */
public class SimulationSeriesGroupMetaData implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private String name;
	private int size;
	private Evaluation evaluation;

	public SimulationSeriesGroupMetaData() {

	}

	public SimulationSeriesGroupMetaData(SimulationSeriesGroup simulation) {

		this.id = simulation.getId();
		this.name = simulation.getName();
		this.size = simulation.size();
		this.evaluation = simulation.getCooperationEvaluation();
		
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public Evaluation getEvaluation() {
		return evaluation;
	}

	public void setId(long id) {
		this.id = id;
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

}
