package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.algorithms.BinarySearchRandomWalkLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.ClizzAlgorithm;
import i5.las2peer.services.ocd.algorithms.CostFunctionOptimizationClusteringAlgorithm;
import i5.las2peer.services.ocd.algorithms.EvolutionaryAlgorithmBasedOnSimilarity;
import i5.las2peer.services.ocd.algorithms.ExtendedSpeakerListenerLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.LinkCommunitiesAlgorithm;
import i5.las2peer.services.ocd.algorithms.MergingOfOverlappingCommunitiesAlgorithm;
import i5.las2peer.services.ocd.algorithms.NISEAlgorithm;
import i5.las2peer.services.ocd.algorithms.OcdAlgorithm;
import i5.las2peer.services.ocd.algorithms.RandomWalkLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.SignedDMIDAlgorithm;
import i5.las2peer.services.ocd.algorithms.SignedProbabilisticMixtureAlgorithm;
import i5.las2peer.services.ocd.algorithms.SpeakerListenerLabelPropagationAlgorithm;
import i5.las2peer.services.ocd.algorithms.SskAlgorithm;
import i5.las2peer.services.ocd.algorithms.WeightedLinkCommunitiesAlgorithm;
import i5.las2peer.services.ocd.algorithms.WordClusteringRefinementAlgorithm;
import i5.las2peer.services.ocd.algorithms.LocalSpectralClusteringAlgorithm;
import i5.las2peer.services.ocd.algorithms.AntColonyOptimizationAlgorithm;
import i5.las2peer.services.ocd.algorithms.LouvainAlgorithm;
import i5.las2peer.services.ocd.algorithms.DetectingOverlappingCommunitiesAlgorithm;
import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmark;
import i5.las2peer.services.ocd.benchmarks.LfrBenchmark;
import i5.las2peer.services.ocd.benchmarks.SignedLfrBenchmark;
import i5.las2peer.services.ocd.utils.EnumDisplayNames;
import i5.las2peer.services.ocd.benchmarks.NewmanBenchmark;
import i5.las2peer.services.ocd.algorithms.FuzzyCMeansSpectralClusteringAlgorithm;
import i5.las2peer.services.ocd.algorithms.WeakCliquePercolationMethodAlgorithm;
import i5.las2peer.services.ocd.algorithms.RankRemovalAlgorithm;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * Cover creation method registry. Contains algorithms, ground truth benchmarks and abstract types.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum CoverCreationType implements EnumDisplayNames {

	/*
	 * Each enum constant is instantiated with a corresponding CoverCreationMethod class object (typically a concrete OcdAlgorithm or GroundTruthBenchmark subclass) and a UNIQUE id.
	 * Abstract types that do not correspond to any algorithm are instantiated with the CoverCreationMethod interface itself.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	/**
	 * Abstract type usable e.g. for importing covers that were calculated externally by other algorithms.
	 * Cannot be used for algorithm instantiation.
	 */
	UNDEFINED ("Undefined", CoverCreationMethod.class, 0),
	/**
	 * Abstract type mainly intended for importing ground truth covers.
	 * Cannot be used for algorithm instantiation.
	 */
	GROUND_TRUTH ("Ground Truth", CoverCreationMethod.class, 1),
	/**
	 * Type corresponding to the RandomWalkLabelPropagationAlgorithm.
	 */
	RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM ("Random Walk Label Propagation Algorithm", RandomWalkLabelPropagationAlgorithm.class, 2),
	/**
	 * Type corresponding to the SpeakerListenerLabelPropagationAlgorithm.
	 */
	SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM ("Speaker Listener Label Propagation Algorithm", SpeakerListenerLabelPropagationAlgorithm.class, 3),
	/**
	 * Type corresponding to the ExtendedSpeakerListenerLabelPropagationAlgorithm.
	 */
	EXTENDED_SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM("Extended Speaker Listener Label Propagation Algorithm", ExtendedSpeakerListenerLabelPropagationAlgorithm.class, 4),
	/**
	 * Type corresponding to the SskAlgorithm.
	 */
	SSK_ALGORITHM ("SSK Algorithm", SskAlgorithm.class, 5),
	/**
	 * Type corresponding to the LinkCommunitiesAlgorithm.
	 */
	LINK_COMMUNITIES_ALGORITHM ("Link Communities Algorithm", LinkCommunitiesAlgorithm.class, 6),
	/**
	 * Type corresponding to the WeightedLinkCommunitiesAlgorithm.
	 */
	WEIGHTED_LINK_COMMUNITIES_ALGORITHM ("Weighted Link Communities Algorithm", WeightedLinkCommunitiesAlgorithm.class, 7),
	/**
	 * Type corresponding to the ClizzAlgorithm.
	 */
	CLIZZ_ALGORITHM ("CliZZ Algorithm", ClizzAlgorithm.class, 8),
	/**
	 * Type corresponding to the MergingOfOverlappingCommunitiesAlgorithm.
	 */
	MERGING_OF_OVERLAPPING_COMMUNITIES_ALGORITHM("Merging Of Overlapping Communities Algorithm", MergingOfOverlappingCommunitiesAlgorithm.class, 9),
	/**
	 * Type corresponding to the BinarySearchRandomWalkLabelPropagationAlgorithm.
	 */
	BINARY_SEARCH_RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM("Binary Search Random Walk Label Propagation Algorithm", BinarySearchRandomWalkLabelPropagationAlgorithm.class, 10),
	/**
	 * Type corresponding to the LfrBenchmark, which is a ground truth benchmark.
	 * Cannot be used for algorithm instantiation.
	 */
	LFR("LFR Benchmark", LfrBenchmark.class, 11),
	/**
	 * Type corresponding to the NewmanBenchmark, which is a ground truth benchmark.
	 * Cannot be used for algorithm instantiation.
	 */
	NEWMAN("Newman Benchmark", NewmanBenchmark.class, 12),
	/**
	 * Type corresponding to the LfrBenchmark, which is a ground truth benchmark.
	 * Cannot be used for algorithm instantiation.
	 */
	SIGNED_LFR("Signed LFR Benchmark", SignedLfrBenchmark.class, 13),
	/**
	 */
	COST_FUNC_OPT_CLUSTERING_ALGORITHM("Cost Function Optimization Clustering Algorithm", CostFunctionOptimizationClusteringAlgorithm.class, 14),
	/**
	 * Type corresponding to the SignedDMIDAlgorithm.
	 */
	SIGNED_DMID_ALGORITHM("Signed DMID Algorithm", SignedDMIDAlgorithm.class, 15),
	/**
	 * Type corresponding to the EvolutionaryAlgorithmBasedOnSimilarity.
	 */	
	EVOLUTIONARY_ALGORITHM_BASED_ON_SIMILARITY("Evolutionary Algorithm Based On Similarity", EvolutionaryAlgorithmBasedOnSimilarity.class,16),
	/**
	 * Type corresponding to the SignedProbabilisticMixtureAlgorithm.
	 */	
	SIGNED_PROBABILISTIC_MIXTURE_ALGORITHM("Signed Probabilistic Mixture Algorithm", SignedProbabilisticMixtureAlgorithm.class,17),
//	/**
//	 * Type corresponding to the SignedDMIDExtendedAlgorithm.
//	 */	
//	SIGNED_DMID_EXTENDED_ALGORITHM(SignedDMIDExtendedAlgorithm.class,17);
	
	/**
	 * Type corresponding to the wordclustering algorithm with refinement.	
	 */
	WORD_CLUSTERING_REF_ALGORITHM("Word Clustering Refinement Algorithm", WordClusteringRefinementAlgorithm.class, 18), 	
	/**
	 * Type corresponding to the AntColonyOptimization algorithm 
	 */
	ANT_COLONY_OPTIMIZATION("Ant Colony Optimization Algorithm", AntColonyOptimizationAlgorithm.class, 19),
	/**
	 * Type corresponding to the LocalSpectralClustering algorithm.	
	 */
	LOCAL_SPECTRAL_CLUSTERING_ALGORITHM("Local Spectral Clustering Algorithm", LocalSpectralClusteringAlgorithm.class, 20),
	
	/**
	 * Type corresponding to the Louvain method algorithm.	
	 */
	LOUVAIN_ALGORITHM("Louvain Algorithm", LouvainAlgorithm.class, 21),
	
	/**
	 * Type corresponding to the DetectingOverlappingCommunities Algorithm.
	 */
	DETECTING_OVERLAPPING_COMMUNITIES_ALGORITHM("Detecting Overlapping Communities Algorithm", DetectingOverlappingCommunitiesAlgorithm.class, 22),

	/**
	 * Type corresponding to the NISE Algorithm.
	 */
	NISE_ALGORITHM("Neighborhood-Inflated Seed Expansion Algorithm", NISEAlgorithm.class, 23),

        /**
	 * Type corresponding to the FuzzyCMeansSpectralClustering Algorithm.
	 */
	FUZZY_C_MEANS_SPECTRAL_CLUSTERING_ALGORITHM("Fuzzy C Means Spectral Clustering Algorithm", FuzzyCMeansSpectralClusteringAlgorithm.class, 24),
	
	/**
	 * Type corresponding to the WeakCliquePercolationMethodAlgorithm Algorithm.
	 */
	WEAK_CLIQUE_PERCOLATION_METHOD_ALGORITHM("Weak Clique Percolation Method Algorithm", WeakCliquePercolationMethodAlgorithm.class, 25),

	/**
	 * Type corresponding to the Rank Removal Algorithm with Iterative Scan.
	 */
	RANK_REMOVAL_AND_ITERATIVE_SCAN_ALGORITHM("Rank Removal Algorithm with Iterative Scan",RankRemovalAlgorithm.class, 26);
	
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
	 * A display name for web frontends and more
	 */
	private final String displayName;
	
	/**
	 * Creates a new instance.
	 * @param displayName Defines the displayName attribute
	 * @param creationMethodClass Defines the creationMethodClass attribute.
	 * @param id Defines the id attribute.
	 */
	private CoverCreationType(String displayName, Class<? extends CoverCreationMethod> creationMethodClass, int id) {
		this.displayName = displayName;
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
	 * Returns the display name of the type.
	 * @return The name.
	 */
	public String getDisplayName() {
		return displayName;
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