package i5.las2peer.services.servicePackage.algorithms;

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

/**
 * A log representation for any OcdAlgorithm.
 * @author Sebastian
 *
 */
@Entity
public class AlgorithmLog {
	
	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String identifierColumnName = "IDENTIFIER";
	private static final String parametersColumnName = "PARAMETERS";
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = idColumnName)
	private long id;
	@Enumerated(EnumType.STRING)
	@Column(name = identifierColumnName)
	private AlgorithmIdentifier identifier;
	@ElementCollection
	@Column(name = parametersColumnName)
	private Map<String, String> parameters;
	
	/*
	 * Only provided for persistence.
	 */
	public AlgorithmLog() {
	}
	
	public AlgorithmLog(AlgorithmIdentifier identifier, Map<String, String> parameters) {
		if(identifier != null) {
			this.identifier = identifier;
		}
		else {
			this.identifier = AlgorithmIdentifier.UNDEFINED;
		}
		if(parameters != null) {
			this.parameters = parameters;
		}
		else {
			this.parameters = new HashMap<String, String>();
		}
	}

	public AlgorithmIdentifier getIdentifier() {
		return identifier;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}
	
}
