package i5.las2peer.services.servicePackage.algorithms;

import java.util.Locale;

/**
 * An enum representation for overlapping community detection algorithms.
 * @author Sebastian
 *
 */
public enum AlgorithmType {

	UNDEFINED (0),
	GROUND_TRUTH (1),
	RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM (2),
	SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM (3),
	SSK_ALGORITHM (4),
	LINK_COMMUNITIES_ALGORITHM (5),
	CLIZZ_ALGORITHM (6);
	
	private final int id;
	
	private AlgorithmType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public static AlgorithmType lookupType(int id) {
        for (AlgorithmType type : AlgorithmType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        return null;
	}
	
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
}
