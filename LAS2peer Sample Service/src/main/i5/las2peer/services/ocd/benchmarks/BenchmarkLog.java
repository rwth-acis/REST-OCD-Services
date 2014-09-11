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

@Entity
public class BenchmarkLog {

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
	@ElementCollection
	private Map<String, String> parameters;
	@Column(name = typeColumnName)
	private int typeId;
	@Column(name = statusIdColumnName)
	private int statusId = ExecutionStatus.COMPLETED.getId();
	/*
	 * Only provided for persistence. 
	 */
	protected BenchmarkLog() {
	}
	
	public BenchmarkLog(BenchmarkType type, Map<String, String> parameters) {
		this.typeId = type.getId();
		if(parameters != null) {
			this.parameters = parameters;
		}
		else {
			this.parameters = new HashMap<String, String>();
		}
	}

	public long getId() {
		return id;
	}

	public BenchmarkType getType() {
		return BenchmarkType.lookupType(this.typeId);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public ExecutionStatus getStatus() {
		return ExecutionStatus.lookupStatus(statusId);
	}
	
	public void setStatus(ExecutionStatus status) {
		this.statusId = status.getId();
	}
	
}
