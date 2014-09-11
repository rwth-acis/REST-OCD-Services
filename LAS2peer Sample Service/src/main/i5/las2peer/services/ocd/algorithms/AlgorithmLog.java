package i5.las2peer.services.ocd.algorithms;

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
 * A log representation for any OcdAlgorithm execution.
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
	private static final String statusIdColumnName = "STATUS";
	
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
	private int statusId = ExecutionStatus.WAITING.getId();
	@ElementCollection
	private Set<Integer> compatibleGraphTypes = new HashSet<Integer>();
	
	/*
	 * Only provided for persistence.
	 */
	protected AlgorithmLog() {
	}
	
	/**
	 * Creates an instance of algorithm log.
	 * @param type The type of the algorithm.
	 * @param parameters The concrete parameters of the algorithm execution.
	 * @param compatibleGraphTypes The graph types which are compatible with the algorithm.
	 */
	public AlgorithmLog(AlgorithmType type, Map<String, String> parameters, Set<GraphType> compatibleGraphTypes) {
		if(type != null) {
			this.typeId = type.getId();
		}
		else {
			this.typeId = AlgorithmType.UNDEFINED.getId();
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

	public AlgorithmType getType() {
		return AlgorithmType.lookupType(typeId);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public long getId() {
		return id;
	}

	public Set<GraphType> getCompatibleGraphTypes() {
		Set<GraphType> compatibleGraphTypes = new HashSet<GraphType>();
		for(int id : this.compatibleGraphTypes) {
			compatibleGraphTypes.add(GraphType.lookupType(id));
		}
		return compatibleGraphTypes;
	}
	
	public ExecutionStatus getStatus() {
		return ExecutionStatus.lookupStatus(statusId);
	}
	
	public void setStatus(ExecutionStatus status) {
		this.statusId = status.getId();
	}
	
}
