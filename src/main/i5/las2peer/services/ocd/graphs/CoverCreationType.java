package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.algorithms.BinarySearchRandomWalkLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.ClizzAlgorithm;
import i5.las2peer.services.ocd.algorithms.CostFunctionOptimizationClusteringAlgorithm;
import i5.las2peer.services.ocd.algorithms.EvolutionaryAlgorithmBasedOnSimilarity;
import i5.las2peer.services.ocd.algorithms.ExtendedSpeakerListenerLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.LinkCommunitiesAlgorithm;
import i5.las2peer.services.ocd.algorithms.MergingOfOverlappingCommunitiesAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.RandomWalkLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.SignedDMIDAlgorithm;
import i5.las2peer.services.ocd.algorithms.SignedProbabilisticMixtureAlgorithm;
import i5.las2peer.services.ocd.algorithms.SpeakerListenerLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.SskAlgorithm;
import i5.las2peer.services.ocd.algorithms.WeightedLinkCommunitiesAlgorithm;
import i5.las2peer.services.ocd.algorithms.WordClusteringRefinementAlgorithm;
import i5.las2peer.services.ocd.algorithms.LocalSpectralClusteringAlgorithm;
import i5.las2peer.services.ocd.algorithms.LouvainAlgorithm;
import i5.las2peer.services.ocd.algorithms.DetectingOverlappingCommunitiesAlgorithm;
import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmark;
import i5.las2peer.services.ocd.benchmarks.LfrBenchmark;
import i5.las2peer.services.ocd.benchmarks.NewmanBenchmark;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * Cover creation method registry. Contains algorithms, ground truth benchmarks and abstract types.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum CoverCreationType {

	/*
	 * Each enum constant is instantiated with a corresponding CoverCreationMethod class object (typically a concrete OcdAlgorithm or GroundTruthBenchmark subclass) and a UNIQUE id.
	 * Abstract types that do not correspond to any algorithm are instantiated with the CoverCreationMethod interface itself.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	/**
	 * Abstract type usable e.g. for importing covers that were calculated externally by other algorithms.
	 * Cannot be used for algorithm instantiation.
	 */
	UNDEFINED (CoverCreationMethod.class, 0),
	/**
	 * Abstract type mainly intended for importing ground truth covers.
	 * Cannot be used for algorithm instantiation.
	 */
	GROUND_TRUTH (CoverCreationMethod.class, 1),
	/**
	 * Type corresponding to the RandomWalkLabelPropagationAlgorithm.
	 */
	RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM (RandomWalkLabelPropagationAlgorithm.class, 2),
	/**
	 * Type corresponding to the SpeakerListenerLabelPropagationAlgorithm.
	 */
	SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM (SpeakerListenerLabelPropagationAlgorithm.class, 3),
	/**
	 * Type corresponding to the ExtendedSpeakerListenerLabelPropagationAlgorithm.
	 */
	EXTENDED_SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM(ExtendedSpeakerListenerLabelPropagationAlgorithm.class, 4),
	/**
	 * Type corresponding to the SskAlgorithm.
	 */
	SSK_ALGORITHM (SskAlgorithm.class, 5),
	/**
	 * Type corresponding to the LinkCommunitiesAlgorithm.
	 */
	LINK_COMMUNITIES_ALGORITHM (LinkCommunitiesAlgorithm.class, 6),
	/**
	 * Type corresponding to the WeightedLinkCommunitiesAlgorithm.
	 */
	WEIGHTED_LINK_COMMUNITIES_ALGORITHM (WeightedLinkCommunitiesAlgorithm.class, 7),
	/**
	 * Type corresponding to the ClizzAlgorithm.
	 */
	CLIZZ_ALGORITHM (ClizzAlgorithm.class, 8),
	/**
	 * Type corresponding to the MergingOfOverlappingCommunitiesAlgorithm.
	 */
	MERGING_OF_OVERLAPPING_COMMUNITIES_ALGORITHM(MergingOfOverlappingCommunitiesAlgorithm.class, 9),
	/**
	 * Type corresponding to the BinarySearchRandomWalkLabelPropagationAlgorithm.
	 */
	BINARY_SEARCH_RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM(BinarySearchRandomWalkLabelPropagationAlgorithm.class, 10),
	/**
	 * Type corresponding to the LfrBenchmark, which is a ground truth benchmark.
	 * Cannot be used for algorithm instantiation.
	 */
	LFR(LfrBenchmark.class, 11),
	/**
	 * Type corresponding to the NewmanBenchmark, which is a ground truth benchmark.
	 * Cannot be used for algorithm instantiation.
	 */
	NEWMAN(NewmanBenchmark.class, 12),
	/**
	 */
	COST_FUNC_OPT_CLUSTERING_ALGORITHM(CostFunctionOptimizationClusteringAlgorithm.class, 13),
	/**
	 * Type corresponding to the SignedDMIDAlgorithm.
	 */
	SIGNED_DMID_ALGORITHM(SignedDMIDAlgorithm.class, 14),
	/**
	 * Type corresponding to the EvolutionaryAlgorithmBasedOnSimilarity.
	 */	
	EVOLUTIONARY_ALGORITHM_BASED_ON_SIMILARITY(EvolutionaryAlgorithmBasedOnSimilarity.class,15),
	/**
	 * Type corresponding to the SignedProbabilisticMixtureAlgorithm.
	 */	
	SIGNED_PROBABILISTIC_MIXTURE_ALGORITHM(SignedProbabilisticMixtureAlgorithm.class,16),
//	/**
//	 * Type corresponding to the SignedDMIDExtendedAlgorithm.
//	 */	
//	SIGNED_DMID_EXTENDED_ALGORITHM(SignedDMIDExtendedAlgorithm.class,17);
	
	/**
	 * Type corresponding to the wordclustering algorithm with refinement.	
	 */
	WORD_CLUSTERING_REF_ALGORITHM(WordClusteringRefinementAlgorithm.class, 17), 
	
	/**
	 * Type corresponding to the LocalSpectralClustering algorithm.	
	 */
	LOCAL_SPECTRAL_CLUSTERING_ALGORITHM(LocalSpectralClusteringAlgorithm.class, 18),
	
	/**
	 * Type corresponding to the Louvain method algorithm.	
	 */
	LOUVAIN_ALGORITHM(LouvainAlgorithm.class, 19),
	
	/**
	 * Type corresponding to the DetectingOverlappingCommunities Algorithm.
	 */
	DETECTING_OVERLAPPING_COMMUNITIES_ALGORITHM(DetectingOverlappingCommunitiesAlgorithm.class, 20);

	/**
	 * The class corresponding to the type, typically a concrete OcdAlgorithm or GroundTruthBenchmark subclass.
	 * Abstract types correspond to the CoverCreationMethod interface itself.
	 */
	private final Class<? extends CoverCreationMethod> creationMethodClass;
	
	/**
	 * For persistence and other purposes.
	 */
	private final int id;
	
	/**
	 * Creates a new instance.
	 * @param creationMethodClass Defines the creationMethodClass attribute.
	 * @param id Defines the id attribute.
	 */
	private CoverCreationType(Class<? extends CoverCreationMethod> creationMethodClass, int id) {
		this.creationMethodClass = creationMethodClass;
		this.id = id;
	}
	
	/**
	 * Returns the CoverCreationMethod subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	public Class<? extends CoverCreationMethod> getCreationMethodClass() {
		return this.creationMethodClass;
	}
	
	/**
	 * Returns the unique id of the type.
	 * @return The id.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns the type corresponding to an id.
	 * @param id The id.
	 * @return The corresponding type.
	 */
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
	public boolean correspondsAlgorithm() {
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
	
	/**
	 * Returns the name of the type written in lower case letters and with any underscores replaced by space characters.
	 */
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
}
