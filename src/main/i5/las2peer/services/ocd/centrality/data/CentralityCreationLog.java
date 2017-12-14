package i5.las2peer.services.ocd.centrality.data;

import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * A log representation for a CentralityCreationMethod.
 *
 */
@Entity
public class CentralityCreationLog {
	
	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String centralityTypeColumnName = "CENTRALITY_TYPE";
	private static final String creationTypeColumnName = "CREATION_TYPE";
	private static final String statusIdColumnName = "STATUS";
	private static final String executionTimeColumnName = "EXECUTION_TIME";
	
	/*
	 * Field names.
	 */
	public static final String STATUS_ID_FIELD_NAME = "statusId";
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = idColumnName)
	private long id;
	/**
	 * Parameters used by the creation method.
	 */
	@ElementCollection
	private Map<String, String> parameters;
	/**
	 * Id of the creation methods corresponding CentralityType.
	 */
	@Column(name = centralityTypeColumnName)
	private int centralityTypeId;
	/**
	 * Id of the corresponding CentralityCreationType.
	 */
	@Column(name = creationTypeColumnName)
	private int creationTypeId;
	/**
	 * The status of the corresponding execution.
	 */
	@Column(name = statusIdColumnName)
	private int statusId = ExecutionStatus.WAITING.getId();
	/**
	 * The execution time of the algorithm
	 */
	@Column(name = executionTimeColumnName)
	private long executionTime;
	/**
	 * The graph types the creation method is compatible with.
	 */
	@ElementCollection
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
			if(creationTypeId == CentralityCreationType.CENTRALITY_MEASURE.getId())
				this.centralityTypeId = ((CentralityMeasureType)centralityType).getId();
			else if(creationTypeId == CentralityCreationType.SIMULATION.getId()) {
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
}
