package i5.las2peer.services.ocd.centrality.data;

import i5.las2peer.services.ocd.graphs.GraphCreationLog;
import i5.las2peer.services.ocd.graphs.GraphType;
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
 * A log representation for a CentralityCreationMethod.
 *
 */

public class CentralityCreationLog {
	
	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String centralityTypeColumnName = "CENTRALITY_TYPE";
	public static final String creationTypeColumnName = "CREATION_TYPE";
	public static final String statusIdColumnName = "STATUS";
	public static final String executionTimeColumnName = "EXECUTION_TIME";
	//ArangoDB
	public static final String collectionName = "centralitycreationlog";
	private static final String parameterColumnName = "PARAMETER";
	private static final String compatibleGraphTypesColumnName = "COMPATIBLE_GRAPH_TYPES";
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
	 * Id of the creation methods corresponding CentralityType.
	 */

	private int centralityTypeId;
	/**
	 * Id of the corresponding CentralityCreationType.
	 */

	private int creationTypeId;
	/**
	 * The status of the corresponding execution.
	 */

	private int statusId = ExecutionStatus.WAITING.getId();
	/**
	 * The execution time of the algorithm
	 */

	private long executionTime;
	/**
	 * The graph types the creation method is compatible with.
	 */

	private Set<Integer> compatibleGraphTypes = new HashSet<Integer>();
	
	/**
	 * Creates a new instance.
	 * Only provided for persistence.
	 */
	protected CentralityCreationLog() {
	}
	
	/**
	 * Creates a new instance.
	 * @param centralityType The corresponding CentralityType.
	 * @param creationType The corresponding CentralityCreationType.
	 * @param parameters The parameters used by the creation method.
	 * @param compatibleGraphTypes The graph types which are compatible with the creation method.
	 */
	public CentralityCreationLog(CentralityType centralityType, CentralityCreationType creationType, Map<String, String> parameters, Set<GraphType> compatibleGraphTypes) {
		if(centralityType != null) {
			if(creationType.getId() == CentralityCreationType.CENTRALITY_MEASURE.getId()) {
				this.centralityTypeId = ((CentralityMeasureType) centralityType).getId();
			}
			else if(creationType.getId()  == CentralityCreationType.SIMULATION.getId()) {
				this.centralityTypeId = ((CentralitySimulationType)centralityType).getId();
			}
			else {
				this.centralityTypeId = 0;
			}
		}
		else {
			this.centralityTypeId = CentralityMeasureType.UNDEFINED.getId();
		}
		if(creationType != null) {
			this.creationTypeId = creationType.getId();
		}
		else {
			this.creationTypeId = CentralityCreationType.UNDEFINED.getId();
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
	 * Returns the centrality maps CentralityType as a String.
	 * @return The centrality type name.
	 */
	public String getCentralityTypeName() {
		if(getCreationType() == CentralityCreationType.CENTRALITY_MEASURE) {
			return CentralityMeasureType.lookupType(centralityTypeId).name();
		}
		else if(getCreationType() == CentralityCreationType.SIMULATION) {
			return CentralitySimulationType.lookupType(centralityTypeId).name();
		}
		else {
			return "UNDEFINED";
		}
	}
	
	/**
	 * Returns the centrality maps CentralityCreationType.
	 * @return The type.
	 */
	public CentralityCreationType getCreationType() {
		return CentralityCreationType.lookupType(creationTypeId);
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
	
	public long getExecutionTime() {
		return executionTime;
	}
	
	public void setExecutionTime(long time) {
		executionTime = time;
	}
	
	//persistence functions
	public void persist(ArangoDatabase db, DocumentCreateOptions opt) {
		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		bd.addAttribute(centralityTypeColumnName, this.centralityTypeId);
		bd.addAttribute(creationTypeColumnName, this.creationTypeId);
		bd.addAttribute(parameterColumnName, this.parameters);
		bd.addAttribute(statusIdColumnName, this.statusId);
		bd.addAttribute(executionTimeColumnName, this.executionTime);
		bd.addAttribute(compatibleGraphTypesColumnName, this.compatibleGraphTypes);
		collection.insertDocument(bd, opt);
		this.key = bd.getKey();
	}
	
	public void updateDB(ArangoDatabase db, String transId) {
		DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);
		
		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		bd.addAttribute(centralityTypeColumnName, this.centralityTypeId);
		bd.addAttribute(creationTypeColumnName, this.creationTypeId);
		bd.addAttribute(parameterColumnName, this.parameters);
		bd.addAttribute(statusIdColumnName, this.statusId);
		bd.addAttribute(executionTimeColumnName, this.executionTime);
		bd.addAttribute(compatibleGraphTypesColumnName, this.compatibleGraphTypes);
		collection.updateDocument(this.key, bd, updateOptions);
	}
	
	public static CentralityCreationLog load(String key, ArangoDatabase db, DocumentReadOptions opt) {	
		CentralityCreationLog ccl = new CentralityCreationLog();
		ArangoCollection collection = db.collection(collectionName);
		
		BaseDocument bd = collection.getDocument(key, BaseDocument.class, opt);
		if (bd != null) {
			ObjectMapper om = new ObjectMapper();
			String centralityTypeString = bd.getAttribute(centralityTypeColumnName).toString();
			String creationTypeString = bd.getAttribute(creationTypeColumnName).toString();
			String statusIdString = bd.getAttribute(statusIdColumnName).toString();
			String executionTimeString = bd.getAttribute(executionTimeColumnName).toString();
			Object objParam = bd.getAttribute(parameterColumnName);
			Object objCompGraph = bd.getAttribute(compatibleGraphTypesColumnName);
			
			ccl.key = key;
			if(objParam != null) {
				ccl.parameters = om.convertValue(objParam, Map.class);
			}		
			if(centralityTypeString != null) {
				ccl.centralityTypeId = Integer.parseInt(centralityTypeString);
			}			
			if(creationTypeString != null) {
				ccl.creationTypeId = Integer.parseInt(creationTypeString);
			}				
			if(statusIdString != null) {
				ccl.statusId = Integer.parseInt(statusIdString);
			}				
			if(executionTimeString != null) {
				ccl.executionTime= Long.parseLong(executionTimeString);
			}
			ccl.compatibleGraphTypes = om.convertValue(objCompGraph, Set.class);		
		}
		else {
			System.out.println("empty CentralityCreationLog document");
		}
		return ccl;
	}
	public String String() {
		String n = System.getProperty("line.separator");
		String ret = "CentralityCreationLog: " + n;
		ret += "Key :             " + this.key +n ;
		ret += "parameters :  " 	+ this.parameters.toString() + n;
		ret += "centralityTypeId :" + this.centralityTypeId + n ; 
		ret += "creationTypeId :  " + this.creationTypeId +n;
		ret += "statusId :        " + this.statusId + n;
		ret += "executionTime :   " + this.executionTime +n;
		ret += "GraphTypes :      " + this.compatibleGraphTypes.toString() + n;
		return ret;
	}
}
