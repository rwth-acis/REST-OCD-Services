package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.graph.GraphType;

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
	
}
