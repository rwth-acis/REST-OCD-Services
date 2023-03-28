package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentUpdateOptions;

import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * A log representation for a cover creation method, i.e. typically an OcdAlgorithm or OcdBenchmark execution.
 * @author Sebastian
 *
 */

public class CoverCreationLog {
	
	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	public static final String typeColumnName = "TYPE";
	public static final String statusIdColumnName = "STATUS";
	
	private static final String parameterColumnName = "PARAMETER";
	private static final String compatibleGraphTypesColumnName = "COMPATIBLEGRAPHTYPES";
	public static final String collectionName = "covercreationlog";
	/*
	 * Field names.
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
	 * Id of the creation methods corresponding cover creation type.
	 */

	private int typeId;
	/**
	 * The status of the corresponding execution.
	 */

	private int statusId = ExecutionStatus.WAITING.getId();
	/**
	 * The graph types the creation method is compatible with.
	 */

	private Set<Integer> compatibleGraphTypes = new HashSet<Integer>();
	
	/**
	 * Creates a new instance.
	 * Only provided for persistence.
	 */
	protected CoverCreationLog() {
	}
	
	/**
	 * Creates a new instance.
	 * @param type The type of creation method.
	 * @param parameters The parameters used by the creation method.
	 * @param compatibleGraphTypes The graph types which are compatible with the creation method.
	 */
	public CoverCreationLog(CoverCreationType type, Map<String, String> parameters, Set<GraphType> compatibleGraphTypes) {
		if(type != null) {
			this.typeId = type.getId();
		}
		else {
			this.typeId = CoverCreationType.UNDEFINED.getId();
		}
		if(parameters != null) {
			this.parameters = parameters;
		}
		else {
			this.parameters = new HashMap<String, String>();
		}
		if(compatibleGraphTypes != null) {
			for(GraphType graphType : compatibleGraphTypes) {
				this.compatibleGraphTypes.add(graphType.getId());
			}
		}
	}

	/**
	 * Returns the type of the corresponding creation method.
	 * @return The type.
	 */
	public CoverCreationType getType() {
		return CoverCreationType.lookupType(typeId);
	}

	/**
	 * Returns the parameters used by the corresponding creation method.
	 * @return A mapping from each parameter name to the parameter value in String format.
	 */
	public Map<String, String> getParameters() {
		return parameters;
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
	 * Returns the graph types the corresponding creation method is compatible with.
	 * @return The graph types.
	 */
	public Set<GraphType> getCompatibleGraphTypes() {
		Set<GraphType> compatibleGraphTypes = new HashSet<GraphType>();
		for(int id : this.compatibleGraphTypes) {
			compatibleGraphTypes.add(GraphType.lookupType(id));
		}
		return compatibleGraphTypes;
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
	public void persist(ArangoDatabase db, String transId) {
		DocumentCreateOptions createOptions = new DocumentCreateOptions().streamTransactionId(transId);
		ArangoCollection collection = db.collection(collectionName);
		
		BaseDocument bd = new BaseDocument();
		bd.addAttribute(typeColumnName, this.typeId);
		bd.addAttribute(statusIdColumnName, this.statusId);
		bd.addAttribute(parameterColumnName, this.parameters); //TODO
		bd.addAttribute(compatibleGraphTypesColumnName, this.compatibleGraphTypes);
		
		collection.insertDocument(bd, createOptions);
		this.key = bd.getKey();
	}
	
	public void updateDB(ArangoDatabase db, String transId) {
		DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);
		
		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		bd.addAttribute(typeColumnName, this.typeId);
		bd.addAttribute(statusIdColumnName, this.statusId);
		bd.addAttribute(parameterColumnName, this.parameters);
		bd.addAttribute(compatibleGraphTypesColumnName, this.compatibleGraphTypes);
		collection.updateDocument(this.key, bd, updateOptions);
	}
	
	public static CoverCreationLog load(String key, ArangoDatabase db, DocumentReadOptions opt) {
		CoverCreationLog ccl = new CoverCreationLog();
		ArangoCollection collection = db.collection(collectionName);
		
		BaseDocument bd = collection.getDocument(key, BaseDocument.class, opt);
		if (bd != null) {
			ObjectMapper om = new ObjectMapper();
			String typeIdString = bd.getAttribute(typeColumnName).toString();
			String statusIdString = bd.getAttribute(statusIdColumnName).toString();
			Object objCompatibleGraphTypes = bd.getAttribute(compatibleGraphTypesColumnName);
			Object objParam = bd.getAttribute(parameterColumnName);
			
			ccl.key = key;	
			if(objParam != null) {
				ccl.parameters = om.convertValue(objParam, Map.class);	
			}
			ccl.typeId = Integer.parseInt(typeIdString);
			ccl.statusId = Integer.parseInt(statusIdString);
			if(objCompatibleGraphTypes != null) {
				ccl.compatibleGraphTypes = om.convertValue(objCompatibleGraphTypes, Set.class);
			}
		}	
		else {
			System.out.println("empty CoverCreationLog document");
		}
		return ccl;
	}
	

	public String String() {
		String n = System.getProperty("line.separator");
		String ret = "CoverCreationLog: " + n;
		ret += "Key :         " + this.key +n ;
		ret += "typeId :      " + this.typeId + n ; 
		ret += "statusId :    " + this.statusId + n;
		ret += "parameters :  " + this.parameters.toString() + n;
		ret += "GraphTypes :  " + this.compatibleGraphTypes.toString() + n;
		return ret;
	}
}
