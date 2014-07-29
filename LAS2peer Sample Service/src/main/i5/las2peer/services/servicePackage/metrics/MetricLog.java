package i5.las2peer.services.servicePackage.metrics;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

@Entity
public class MetricLog {

	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String typeColumnName = "TYPE";
	private static final String parametersColumnName = "PARAMETERS";
	private static final String valueColumnName = "VALUE";
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = idColumnName)
	private long id;
	@Transient
	private MetricType type;
	@ElementCollection
	@Column(name = parametersColumnName)
	private Map<String, String> parameters;
	@Column(name = valueColumnName)
	private double value;
	@Column(name = typeColumnName)
	private int typeId;
	/*
	 * Only provided for persistence. 
	 */
	protected MetricLog() {
	}
	
	public MetricLog(MetricType type, double value, Map<String, String> parameters) {
		if(type != null) {
			this.type = type;
		}
		else {
			this.type = MetricType.UNDEFINED;
		}
		if(parameters != null) {
			this.parameters = parameters;
		}
		else {
			this.parameters = new HashMap<String, String>();
		}
		this.value = value;
	}

	public long getId() {
		return id;
	}

	public MetricType getType() {
		return type;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public double getValue() {
		return value;
	}
	
	@PrePersist
	@PreUpdate
	private void prePersist() {
		this.typeId = this.type.getId();
	}
	
	@PostLoad
	private void postLoad() {
		this.type = MetricType.lookupType(this.typeId);
	}
	
}
