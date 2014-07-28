package i5.las2peer.services.servicePackage.metrics;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MetricLog {

	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String identifierColumnName = "IDENTIFIER";
	private static final String parametersColumnName = "PARAMETERS";
	private static final String valueColumnName = "VALUE";
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = idColumnName)
	private long id;
	@Enumerated(EnumType.STRING)
	@Column(name = identifierColumnName)
	private MetricIdentifier identifier;
	@ElementCollection
	@Column(name = parametersColumnName)
	private Map<String, String> parameters;
	@Column(name = valueColumnName)
	private double value;
	
	/*
	 * Only provided for persistence. 
	 */
	protected MetricLog() {
	}
	
	public MetricLog(MetricIdentifier identifier, double value, Map<String, String> parameters) {
		if(identifier != null) {
			this.identifier = identifier;
		}
		else {
			this.identifier = MetricIdentifier.UNDEFINED;
		}
		if(parameters != null) {
			this.parameters = parameters;
		}
		else {
			this.parameters = new HashMap<String, String>();
		}
		this.value = value;
	}

	public MetricIdentifier getIdentifier() {
		return identifier;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public double getValue() {
		return value;
	}
	
}
