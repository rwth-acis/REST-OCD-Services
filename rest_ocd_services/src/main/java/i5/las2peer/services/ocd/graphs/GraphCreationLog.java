package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * A log representation for a graph creation method, i.e. typically a OcdBenchmark execution.
 * @author Sebastian
 *
 */

public class GraphCreationLog {

	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	public static final String typeColumnName = "TYPE";
	public static final String statusIdColumnName = "STATUS";
	
	private static final String parameterColumnName = "PARAMETER";
	public static final String collectionName = "graphcreationlog";
	/*
	 * Field names
	 */	
	public static final String STATUS_ID_FIELD_NAME = "statusId";
	
	/**
	 * System generated persistence id.
	 */


	private long id;
	/**
	 * System generated persistence key.
	 */
	private String key;
	/**
	 * Parameters used by the creation method.
	 */

	private Map<String, String> parameters;
	/**
	 * Id of the creation methods corresponding graph creation type.
	 */

	private int typeId;
	/**
	 * The status of the corresponding execution.
	 */

	private int statusId = ExecutionStatus.COMPLETED.getId();
	
	/**
	 * Creates a new instance.
	 * Only provided for persistence. 
	 */
	protected GraphCreationLog() {
	}
	
	/**
	 * Creates a new instance.
	 * @param type The type of the corresponding creation method.
	 * @param parameters The parameters used by the creation method.
	 */
	public GraphCreationLog(GraphCreationType type, Map<String, String> parameters) {
		this.typeId = type.getId();
		if(parameters != null) {
			this.parameters = parameters;
		}
		else {
			this.parameters = new HashMap<String, String>();
		}
	}

	/**
	 * Returns the log id.
	 * @return The id.
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Returns the log key.
	 * @return The key.
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Returns the type of the corresponding creation method.
	 * @return The type.
	 */
	public GraphCreationType getType() {
		return GraphCreationType.lookupType(this.typeId);
	}

	/**
	 * Returns the parameters used by the corresponding creation method.
	 * @return A mapping from each parameter name to the corresponding value in String format.
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	/**
	 * Returns the execution status of the corresponding creation method.
	 * @return The status.
	 */
	public ExecutionStatus getStatus() {
		return ExecutionStatus.lookupStatus(statusId);
	}
	
	/**
	 * Sets the execution status of the corresponding creation method.
	 * @param status The status.
	 */
	public void setStatus(ExecutionStatus status) {
		this.statusId = status.getId();
	}
	
	//persistence functions
	public void persist(ArangoDatabase db, DocumentCreateOptions opt) {
		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		bd.addAttribute(typeColumnName, this.typeId);
		bd.addAttribute(statusIdColumnName, this.statusId);
		bd.addAttribute(parameterColumnName, this.parameters); 
		
		collection.insertDocument(bd, opt);
		this.key = bd.getKey();
	}
	
	public void updateDB(ArangoDatabase db, String transId) {
		DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);
		
		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		bd.addAttribute(typeColumnName, this.typeId);
		bd.addAttribute(statusIdColumnName, this.statusId);
		bd.addAttribute(parameterColumnName, this.parameters);

		collection.updateDocument(this.key, bd, updateOptions);
	}
	
	public static GraphCreationLog load(String key, ArangoDatabase db, DocumentReadOptions opt) {	
		GraphCreationLog gcl = new GraphCreationLog();
		ArangoCollection collection = db.collection(collectionName);
		
		BaseDocument bd = collection.getDocument(key, BaseDocument.class, opt);
		if (bd != null) {
			ObjectMapper om = new ObjectMapper();
			String typeIdString = bd.getAttribute(typeColumnName).toString();
			int typeId = Integer.parseInt(typeIdString);
			String statusIdString = bd.getAttribute(statusIdColumnName).toString();
			int statusId = Integer.parseInt(statusIdString);
			Object obj = bd.getAttribute(parameterColumnName);
			
			gcl.typeId = typeId;
			gcl.statusId = statusId;
			gcl.key = key;
			gcl.parameters = om.convertValue(obj, Map.class);	
		}	
		else {
			System.out.println("Empty Document");
		}
		return gcl;
	}

	public String String() {
		String n = System.getProperty("line.separator");
		String ret = "GraphCreationLog: " + n;
		ret += "Key :			" + this.key + n;
		ret += "typeId : 		" + this.typeId + n; 
		ret += "statusId : 		" + this.statusId + n;
		ret += "parameters :    " + this.parameters.toString() + n;
		return ret;
	}
	
}
