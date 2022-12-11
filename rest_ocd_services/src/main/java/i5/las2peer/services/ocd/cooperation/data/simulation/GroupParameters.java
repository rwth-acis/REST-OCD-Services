package i5.las2peer.services.ocd.cooperation.data.simulation;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

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

@Entity
public class GroupParameters implements TableLineInterface {

	//ArangoDB
	public static final String collectionName = "groupparameters";
	public static final String simulationSeriesGroupKeyColumnName = "SIMULATION_SERIES_GROUP_KEY";
	public static final String graphKeyColumnName = "GRAPH_KEY";
	private static final String gameTypeColumnName = "GAME_TYPE";
	private static final String scalingColumnName = "SCALING";
	private static final String dynamicTypeColumnName = "DYNAMIC_TYPE";

	////////// Entity Fields //////////

	@JsonProperty
	private String graphKey;

	//@Enumerated(EnumType.STRING)
	@JsonProperty
	private GroupType game; //TODO: why is this GroupType and not GameType?

	//@Basic
	@JsonProperty
	private int scaling;

	//@Enumerated(EnumType.STRING)
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



	/**
	 * Update column values to be stored in the database.
	 * @param bd       Document holding updated values.
	 * @return         Document with updated values.
	 */
	public BaseDocument updateDocument(BaseDocument bd){
		//bd.addAttribute(simulationSeriesGroupKeyColumnName, this.getSimulationSeriesGroupKey());
		bd.addAttribute(graphKeyColumnName, this.getGraphKey());
		bd.addAttribute(gameTypeColumnName, this.getGame().humanRead());
		bd.addAttribute(scalingColumnName, this.getScaling());
		bd.addAttribute(dynamicTypeColumnName, this.getDynamic().humanRead());
		return bd;
	}

	// Persistence Methods
	public void persist(ArangoDatabase db, String transId) {
		ArangoCollection collection = db.collection(collectionName);
		DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);
		BaseDocument bd = new BaseDocument();
		updateDocument(bd);
		collection.insertDocument(bd, createOptions);
		//this.key = bd.getKey(); // if key is assigned before inserting (line above) the value is null
	}

	public void updateDB(ArangoDatabase db, String transId) {
		DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);

		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		updateDocument(bd);
		//collection.updateDocument(this.key, bd, updateOptions);
	}

	public static GroupParameters load(String key, ArangoDatabase db, String transId) {
		GroupParameters groupParameters = new GroupParameters();
		ArangoCollection collection = db.collection(collectionName);

		BaseDocument bd = collection.getDocument(key, BaseDocument.class);
		if (bd != null) {
			//groupParameters.setKey(bd.getKey());
			groupParameters.setGraphKey(bd.getAttribute(graphKeyColumnName).toString());
			groupParameters.setGame(bd.getAttribute(gameTypeColumnName).toString());
			groupParameters.setScaling((int) bd.getAttribute(scalingColumnName));
			groupParameters.setDynamic(bd.getAttribute(dynamicTypeColumnName).toString());

		}
		else {
			System.out.println("GroupParameters with key " + key + " not found.");
		}
		return groupParameters;
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
