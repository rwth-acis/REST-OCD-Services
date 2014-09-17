package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmark;
import i5.las2peer.services.ocd.benchmarks.LfrBenchmark;
import i5.las2peer.services.ocd.benchmarks.NewmanBenchmark;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * Cover creation method registry.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum CoverCreationType {

	/*
	 * Each enum constant is instantiated with a corresponding OcdAlgorithm class object and a UNIQUE id.
	 * Abstract types that do not correspond to any algorithm are instantiated with the CoverCreationMethod interface itself.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	UNDEFINED (CoverCreationMethod.class, 0),
	GROUND_TRUTH (CoverCreationMethod.class, 1),
	RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM (RandomWalkLabelPropagationAlgorithm.class, 2),
	SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM (SpeakerListenerLabelPropagationAlgorithm.class, 3),
	EXTENDED_SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM(ExtendedSpeakerListenerLabelPropagationAlgorithm.class, 4),
	SSK_ALGORITHM (SskAlgorithm.class, 5),
	LINK_COMMUNITIES_ALGORITHM (LinkCommunitiesAlgorithm.class, 6),
	WEIGHTED_LINK_COMMUNITIES_ALGORITHM (WeightedLinkCommunitiesAlgorithm.class, 7),
	CLIZZ_ALGORITHM (ClizzAlgorithm.class, 8),
	MERGING_OF_OVERLAPPING_COMMUNITIES_ALGORITHM(MergingOfOverlappingCommunitiesAlgorithm.class, 9),
	BINARY_SEARCH_RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM(BinarySearchRandomWalkLabelPropagationAlgorithm.class, 10),
	LFR(LfrBenchmark.class, 11),
	NEWMAN(NewmanBenchmark.class, 12);
	
	private final Class<? extends CoverCreationMethod> creationMethodClass;
	
	private final int id;
	
	private CoverCreationType(Class<? extends CoverCreationMethod> algorithmClass, int id) {
		this.creationMethodClass = algorithmClass;
		this.id = id;
	}
	
	protected Class<? extends CoverCreationMethod> getCreationMethodClass() {
		return this.creationMethodClass;
	}
	
	public int getId() {
		return id;
	}
	
	public static CoverCreationType lookupType(int id) {
        for (CoverCreationType type : CoverCreationType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        throw new InvalidParameterException();
	}
	
	/**
	 * States whether the corresponding creation method class is actually an OcdAlgorithm.
	 * @return TRUE if the class is an OcdAlgorithm, otherwise FALSE.
	 */
	public boolean isAlgorithm() {
		if(OcdAlgorithm.class.isAssignableFrom(this.getCreationMethodClass())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * States whether the corresponding CoverCreationMethod class is a ground truth benchmark.
	 * @return TRUE if the class is a ground truth benchmark, otherwise FALSE.
	 */
	public boolean correspondsGroundTruthBenchmark() {
		if(GroundTruthBenchmark.class.isAssignableFrom(this.getCreationMethodClass())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
}
