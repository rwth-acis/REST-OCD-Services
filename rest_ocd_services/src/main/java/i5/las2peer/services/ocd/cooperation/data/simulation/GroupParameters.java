package i5.las2peer.services.ocd.cooperation.data.simulation;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import i5.las2peer.services.ocd.cooperation.data.table.TableLineInterface;
import i5.las2peer.services.ocd.cooperation.data.table.TableRow;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.DynamicType;


public class GroupParameters implements TableLineInterface {

	@JsonProperty
	private String graphKey;

	@JsonProperty
	private GroupType game; //TODO: why is this GroupType and not GameType?

	@JsonProperty
	private int scaling;

	@JsonProperty
	private DynamicType dynamic;

	////////// Constructor //////////

	public GroupParameters() {

	}

	public GroupType getGame() {
		return game;
	}

	public int getScaling() {
		return scaling;
	}

	public DynamicType getDynamic() {
		return dynamic;
	}

	public String getGraphKey() {
		return graphKey;
	}

	public void setGraphKey(String graphKey) {
		this.graphKey = graphKey;
	}

	public void setGame(GroupType game) {
		this.game = game;
	}

	public void setGame(String game) {
		this.game = GroupType.fromString(game);
	}	

	public void setScaling(int scaling) {
		this.scaling = scaling;
	}

	public void setDynamic(DynamicType dynamic) {
		this.dynamic = dynamic;
	}
	

	public void setDynamic(String dynamic) {
		this.dynamic = DynamicType.fromString(dynamic);
	}
	
	////////// Validate //////////
	
	public boolean validate() {
		
		if(getGraphKey() == "0")
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

	@Override
	public String toString() {
		return "GroupParameters{" +
				", graphKey='" + graphKey + '\'' +
				", game=" + game +
				", scaling=" + scaling +
				", dynamic=" + dynamic +
				'}';
	}
}
