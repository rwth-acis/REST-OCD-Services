package i5.las2peer.services.ocd.algorithms;

import java.security.InvalidParameterException;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Representation of overlapping community detection algorithms.
 * @author Sebastian
 *
 */
public enum AlgorithmType {

	UNDEFINED (0),
	GROUND_TRUTH (1),
	RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM (2),
	SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM (3),
	EXTENDED_SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM(4),
	SSK_ALGORITHM (5),
	LINK_COMMUNITIES_ALGORITHM (6),
	WEIGHTED_LINK_COMMUNITIES_ALGORITHM (7),
	CLIZZ_ALGORITHM (8),
	MERGING_OF_OVERLAPPING_COMMUNITIES_ALGORITHM(9),
	BINARY_SEARCH_RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM(10);
	
	private OcdAlgorithm getAlgorithmInstance() {
		switch (this) {
			case UNDEFINED:
			case GROUND_TRUTH:
				throw new IllegalStateException("This is not an actual algorithm.");
			case RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM:
				return new RandomWalkLabelPropagationAlgorithm();
			case SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM:
				return new SpeakerListenerLabelPropagationAlgorithm();
			case EXTENDED_SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM:
				return new ExtendedSpeakerListenerLabelPropagationAlgorithm();
			case SSK_ALGORITHM:
				return new SskAlgorithm();
			case LINK_COMMUNITIES_ALGORITHM:
				return new LinkCommunitiesAlgorithm();
			case WEIGHTED_LINK_COMMUNITIES_ALGORITHM:
				return new WeightedLinkCommunitiesAlgorithm();
			case CLIZZ_ALGORITHM:
				return new ClizzAlgorithm();
			case MERGING_OF_OVERLAPPING_COMMUNITIES_ALGORITHM:
				return new MergingOfOverlappingCommunitiesAlgorithm();
			case BINARY_SEARCH_RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM:
				return new BinarySearchRandomWalkLabelPropagationAlgorithm();
			default:
				throw new NotImplementedException("Ocd algorithm not registered.");
		}
	}
	
	public OcdAlgorithm getAlgorithmInstance(Map<String, String> parameters) {
		OcdAlgorithm algorithm = getAlgorithmInstance();
		algorithm.setParameters(parameters);
		return algorithm;
	}
	
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
        throw new InvalidParameterException();
	}
	
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
}
