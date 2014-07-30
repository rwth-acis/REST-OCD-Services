package i5.las2peer.services.servicePackage.metrics;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MetricLog {

	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String typeColumnName = "TYPE";
	private static final String valueColumnName = "VALUE";
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = idColumnName)
	private long id;
	@ElementCollection
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
			this.typeId = type.getId();
		}
		else {
			this.typeId = MetricType.UNDEFINED.getId();
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
		return MetricType.lookupType(this.typeId);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public double getValue() {
		return value;
	}
	
}
