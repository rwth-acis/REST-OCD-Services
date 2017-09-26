package i5.las2peer.services.ocd.cd.data.simulation;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import i5.las2peer.services.ocd.cd.data.table.TableLineInterface;
import i5.las2peer.services.ocd.cd.data.table.TableRow;
import i5.las2peer.services.ocd.cd.simulation.dynamic.DynamicType;

@Entity
public class GroupParameters implements TableLineInterface {

	////////// Entity Fields //////////

	@Id
	@OneToOne(fetch = FetchType.EAGER)
	SimulationSeriesGroup simulations;

	@Basic
	private long graphId;

	@Enumerated(EnumType.STRING)
	private GroupType game;

	@Basic
	private int scaling;

	@Enumerated(EnumType.STRING)
	private DynamicType dynamic;

	////////// Constructor //////////

	public GroupParameters() {

	}
	////////// Getter //////////
	
	@JsonIgnore
	public SimulationSeriesGroup getSimulations() {
		return simulations;
	}
	
	@JsonProperty
	public long getGraphId() {
		return graphId;
	}
	
	@JsonProperty
	public GroupType getGame() {
		return game;
	}
	
	@JsonProperty
	public int getScaling() {
		return scaling;
	}
	
	@JsonProperty
	public DynamicType getDynamic() {
		return dynamic;
	}

	////////// Setter //////////
	
	@JsonIgnore
	public void setSimulations(SimulationSeriesGroup simulations) {
		this.simulations = simulations;
	}
	
	@JsonSetter
	public void setGraphId(long graphId) {
		this.graphId = graphId;
	}
	
	@JsonIgnore
	public void setGame(GroupType game) {
		this.game = game;
	}
	
	@JsonSetter
	public void setGame(String game) {
		this.game = GroupType.fromString(game);
	}	
	
	@JsonSetter
	public void setScaling(int scaling) {
		this.scaling = scaling;
	}
	
	@JsonIgnore
	public void setDynamic(DynamicType dynamic) {
		this.dynamic = dynamic;
	}
	
	@JsonSetter
	public void setDynamic(String dynamic) {
		this.dynamic = DynamicType.fromString(dynamic);
	}
	
	////////// Validate //////////
	
	public boolean validate() {
		
		if(getGraphId() == 0)
			return false;
		if(getGame() == null)
			return false;
		if(getDynamic() == null)
			return false;
		if(getScaling() < 1)
			return false;
		return true;
	}	
	
	///////// Print /////////

	@Override
	public TableRow toTableLine() {
		
		TableRow line = new TableRow();
		line.add("");
		return line;

	}

	public TableRow toHeadLine() {

		TableRow line = new TableRow();
		line.add("");
		return line;
	}

}
