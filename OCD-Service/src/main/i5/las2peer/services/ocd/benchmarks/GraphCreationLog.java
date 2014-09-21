package i5.las2peer.services.ocd.benchmarks;

import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * A log representation for a graph creation method, i.e. typically a OcdBenchmark execution.
 * @author Sebastian
 *
 */
@Entity
public class GraphCreationLog {

	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String typeColumnName = "TYPE";
	private static final String statusIdColumnName = "STATUS";
	
	/*
	 * Field names
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
	 * Id of the creation methods corresponding graph creation type.
	 */
	@Column(name = typeColumnName)
	private int typeId;
	/**
	 * The status of the corresponding execution.
	 */
	@Column(name = statusIdColumnName)
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
	
}
