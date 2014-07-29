package i5.las2peer.services.servicePackage.algorithms;

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
	private static final String typeColumnName = "TYPE";
	private static final String parametersColumnName = "PARAMETERS";
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = idColumnName)
	private long id;
	@Transient
	private AlgorithmType type;
	@ElementCollection
	@Column(name = parametersColumnName)
	private Map<String, String> parameters;
	@Column(name = typeColumnName)
	private int typeId;
	
	/*
	 * Only provided for persistence.
	 */
	public AlgorithmLog() {
	}
	
	public AlgorithmLog(AlgorithmType type, Map<String, String> parameters) {
		if(type != null) {
			this.type = type;
		}
		else {
			this.type = AlgorithmType.UNDEFINED;
		}
		if(parameters != null) {
			this.parameters = parameters;
		}
		else {
			this.parameters = new HashMap<String, String>();
		}
	}

	public AlgorithmType getType() {
		return type;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public long getId() {
		return id;
	}
	
	@PrePersist
	@PreUpdate
	private void prePersist() {
		this.typeId = this.type.getId();
	}
	
	@PostLoad
	private void postLoad() {
		this.type = AlgorithmType.lookupType(this.typeId);
	}
		
}
