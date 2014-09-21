package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

/**
 * A log representation for an OcdMetric execution.
 * @author Sebastian
 *
 */
@Entity
@IdClass(OcdMetricLogId.class)
public class OcdMetricLog {

	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String typeColumnName = "TYPE";
	private static final String valueColumnName = "VALUE";
	public static final String coverIdColumnName = "COVER_ID";
	public static final String graphIdColumnName = "GRAPH_ID";
	public static final String graphUserColumnName = "USER_NAME";
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
	 * The cover the metric was run on.
	 */
	@Id
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name=graphIdColumnName, referencedColumnName=Cover.graphIdColumnName),
		@JoinColumn(name=graphUserColumnName, referencedColumnName=Cover.graphUserColumnName),
		@JoinColumn(name=coverIdColumnName, referencedColumnName=Cover.idColumnName)
	})
	private Cover cover;
	/**
	 * Parameters used by the metric.
	 */
	@ElementCollection
	private Map<String, String> parameters;
	/**
	 * The calculated metric value.
	 */
	@Column(name = valueColumnName)
	private double value;
	/**
	 * Id of the metrics corresponding ocd metric type.
	 */
	@Column(name = typeColumnName)
	private int typeId;
	/**
	 * The status of the corresponding execution.
	 */
	@Column(name = statusIdColumnName)
	private int statusId = ExecutionStatus.WAITING.getId();
	
	/**
	 * Creates a new instance.
	 * Only provided for persistence. 
	 */
	protected OcdMetricLog() {
	}
	
	/**
	 * Creates a new instance.
	 * @param type The type of the corresponding metric.
	 * @param value The value calculated by the metric.
	 * @param parameters The parameters used by the metric.
	 * @param cover The cover the metric was run on.
	 */
	public OcdMetricLog(OcdMetricType type, double value, Map<String, String> parameters, Cover cover) {
		if(type != null) {
			this.typeId = type.getId();
		}
		else {
			this.typeId = OcdMetricType.UNDEFINED.getId();
		}
		if(parameters != null) {
			this.parameters = parameters;
		}
		else {
			this.parameters = new HashMap<String, String>();
		}
		this.value = value;
		this.cover = cover;
	}

	/**
	 * Returns the log id.
	 * @return The id.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Returns the type of the corresponding metric.
	 * @return The type.
	 */
	public OcdMetricType getType() {
		return OcdMetricType.lookupType(this.typeId);
	}

	/**
	 * Returns the parameters used by the corresponding metric.
	 * @return A mapping from each parameter name to the corresponding value in String format.
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Returns the value calculated by the corresponding metric.
	 * @return The value.
	 */
	public double getValue() {
		return value;
	}
	
	/**
	 * Sets the corresponding metric value.
	 * @param value The value.
	 */
	public void setValue(double value) {
		this.value = value;
	}
	
	/**
	 * Returns the execution status of the corresponding metric.
	 * @return The status.
	 */
	public ExecutionStatus getStatus() {
		return ExecutionStatus.lookupStatus(statusId);
	}
	
	/**
	 * Sets the execution status of the corresponding metric.
	 * @param status The status.
	 */
	public void setStatus(ExecutionStatus status) {
		this.statusId = status.getId();
	}
	
	/**
	 * Returns the cover the corresponding metric was run on.
	 * @return The cover.
	 */
	public Cover getCover() {
		return cover;
	}
	
}
