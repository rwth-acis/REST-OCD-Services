package i5.las2peer.services.ocd.centrality.data;

import java.security.InvalidParameterException;
import java.util.Locale;

import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;

public enum CentralityMeasureType implements CentralityType {
	
	UNDEFINED("Undefined", CentralityCreationMethod.class, 0),
	
	/**
	 * Type corresponding to degree centrality
	 */
	DEGREE_CENTRALITY("Degree Centrality", i5.las2peer.services.ocd.centrality.measures.DegreeCentrality.class, 1),
	
	/**
	 * Type corresponding to in-degree
	 */
	IN_DEGREE("In-degree", i5.las2peer.services.ocd.centrality.measures.InDegree.class, 2),
	
	/**
	 * Type corresponding to out-degree
	 */
	OUT_DEGREE("Out-degree", i5.las2peer.services.ocd.centrality.measures.OutDegree.class, 3),
	
	/**
	 * Type corresponding to LocalRank
	 */
	LOCAL_RANK("LocalRank", i5.las2peer.services.ocd.centrality.measures.LocalRank.class, 4),
	
	/**
	 * Type corresponding to ClusterRank
	 */
	CLUSTER_RANK("ClusterRank", i5.las2peer.services.ocd.centrality.measures.ClusterRank.class, 5),
	
	/**
	 * Type corresponding to Corensess
	 */
	CORENESS("Coreness", i5.las2peer.services.ocd.centrality.measures.Coreness.class, 6),
	
	/**
	 * Type corresponding to the neighborhood coreness
	 */
	NEIGHBORHOOD_CORENESS("Neighborhood Coreness", i5.las2peer.services.ocd.centrality.measures.NeighborhoodCoreness.class, 7),
	
	/**
	 * Type corresponding to H-Index
	 */
	H_INDEX("H-index", i5.las2peer.services.ocd.centrality.measures.HIndex.class, 8),
	
	/**
	 * Type corresponding to laplacian centrality
	 */
	LAPLACIAN_CENTRALITY("Laplacian Centrality", i5.las2peer.services.ocd.centrality.measures.LaplacianCentrality.class, 9),
	
	/**
	 * Type corresponding to eccentricity
	 */
	ECCENTRICITY("Eccentricity", i5.las2peer.services.ocd.centrality.measures.Eccentricity.class, 10),
	
	/**
	 * Type corresponding to closeness centrality
	 */
	CLOSENESS_CENTRALITY("Closeness Centrality", i5.las2peer.services.ocd.centrality.measures.ClosenessCentrality.class, 11),
	
	/**
	 * Type corresponding to harmonic centrality
	 */
	HARMONIC_CENTRALITY("Harmonic Centrality", i5.las2peer.services.ocd.centrality.measures.HarmonicCentrality.class, 12),
	
	/**
	 * Type corresponding to variant of harmonic centrality
	 */
	HARMONIC_IN_CLOSENESS("Harmonic In-Closeness", i5.las2peer.services.ocd.centrality.measures.HarmonicInCloseness.class, 13),
	
	/**
	 * Type corresponding to the current flow closeness centrality
	 */
	CURRENT_FLOW_CLOSENESS("Current-Flow Closeness", i5.las2peer.services.ocd.centrality.measures.CurrentFlowCloseness.class, 14),
	
	/**
	 * Type corresponding to integration
	 */
	INTEGRATION("Integration", i5.las2peer.services.ocd.centrality.measures.Integration.class, 15),
	
	/**
	 * Type corresponding to radiality
	 */
	RADIALITY("Radiality", i5.las2peer.services.ocd.centrality.measures.Radiality.class, 16),
	
	/**
	 * Type corresponding to the residual closeness
	 */
	RESIDUAL_ClOSENESS("Residual Closeness", i5.las2peer.services.ocd.centrality.measures.ResidualCloseness.class, 17),
	
	/**
	 * Type corresponding to the centroid value
	 */
	CENTROID_VALUE("Centroid Value", i5.las2peer.services.ocd.centrality.measures.CentroidValue.class, 18),
	
	/**
	 * Type corresponding to the stress centrality
	 */
	STRESS_CENTRALITY("Stress Centrality", i5.las2peer.services.ocd.centrality.measures.StressCentrality.class, 19),
	
	/**
	 * Type corresponding to betweenness centrality
	 */
	BETWEENNESS_CENTRALITY("Betweenness Centrality", i5.las2peer.services.ocd.centrality.measures.BetweennessCentrality.class, 20),
	
	/**
	 * Type corresponding to the current flow betweenness centrality
	 */
	CURRENT_FLOW_BETWEENNESS("Current-Flow Betweenness", i5.las2peer.services.ocd.centrality.measures.CurrentFlowBetweenness.class, 21),
	
	/**
	 * Type corresponding to flow betweenness
	 */
	FLOW_BETWEENNESS("Flow Betweenness", i5.las2peer.services.ocd.centrality.measures.FlowBetweenness.class, 22),
	
	/**
	 * Type corresponding to the bridging coefficient
	 */
	BRIDGING_COEFFICIENT("Bridging Coefficient", i5.las2peer.services.ocd.centrality.measures.BridgingCoefficient.class, 23),
	
	/**
	 * Type corresponding to the bridging centrality
	 */
	BRIDGING_CENTRALITY("Bridging Centrality", i5.las2peer.services.ocd.centrality.measures.BridgingCentrality.class, 24),
	
	/**
	 * Type corresponding to Katz centrality
	 */
	KATZ_CENTRALITY("Katz Centrality", i5.las2peer.services.ocd.centrality.measures.KatzCentrality.class, 25),
	
	/**
	 * Type corresponding to subgraph centrality
	 */
	SUBGRAPH_CENTRALITY("Subgraph Centrality", i5.las2peer.services.ocd.centrality.measures.SubgraphCentrality.class, 26),
	
	/**
	 * Type corresponding to eigenvector centrality
	 */
	EIGENVECTOR_CENTRALITY("Eigenvector Centrality", i5.las2peer.services.ocd.centrality.measures.EigenvectorCentrality.class, 27),
	
	/**
	 * Type corresponding to the alpha centrality
	 */
	ALPHA_CENTRALITY("Alpha Centrality", i5.las2peer.services.ocd.centrality.measures.AlphaCentrality.class, 28),
	
	/**
	 * Type corresponding to the bargaining centrality
	 */
	BARGAINING_CENTRALITY("Bargaining Centrality", i5.las2peer.services.ocd.centrality.measures.BargainingCentrality.class, 29),
	
	/**
	 * Type corresponding to PageRank
	 */
	PAGERANK("PageRank", i5.las2peer.services.ocd.centrality.measures.PageRank.class, 30),
	
	/**
	 * Type corresponding to alpha centrality
	 */
	LEADERRANK("LeaderRank", i5.las2peer.services.ocd.centrality.measures.LeaderRank.class, 31),
	
	/**
	 * Type corresponding to the hyperlink-induced topic search (HITS) hub score
	 */
	HITS_HUB_SCORE("HITS (Hub Score)", i5.las2peer.services.ocd.centrality.measures.HitsHubScore.class, 32),
	
	/**
	 * Type corresponding to the hyperlink-induced topic search (HITS) authority score
	 */
	HITS_AUTHORITY_SCORE("HITS (Authority Score)", i5.las2peer.services.ocd.centrality.measures.HitsAuthorityScore.class, 33),
	
	/**
	 * Type corresponding to the SALSA hub score
	 */
	SALSA_HUB_SCORE("SALSA (Hub Score)", i5.las2peer.services.ocd.centrality.measures.SalsaHubScore.class, 34),
	
	/**
	 * Type corresponding to the SALSA authority score
	 */
	SALSA_AUTHORITY_SCORE("SALSA (Authority Score)", i5.las2peer.services.ocd.centrality.measures.SalsaAuthorityScore.class, 35);
	
	/**
	 * The class corresponding to the type
	 */
	private final Class<? extends CentralityCreationMethod> creationMethodClass;
	
	/**
	 * For persistence and other purposes.
	 */
	private final int id;
	
	private final String displayName;
	
	/**
	 * Creates a new instance.
	 * @param creationMethodClass Defines the creationMethodClass attribute.
	 * @param id Defines the id attribute.
	 */
	private CentralityMeasureType(String displayName, Class<? extends CentralityCreationMethod> creationMethodClass, int id) {
		this.displayName = displayName;
		this.creationMethodClass = creationMethodClass;
		this.id = id;
	}
	
	/**
	 * Returns the CentralityCreationMethod subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	public Class<? extends CentralityCreationMethod> getCreationMethodClass() {
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
	public static CentralityMeasureType lookupType(int id) {
        for (CentralityMeasureType type : CentralityMeasureType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        throw new InvalidParameterException();
	}
	
	/**
	 * Returns the type corresponding to the given display name.
	 * @param displayName The display name.
	 * @return The corresponding type.
	 */
	public static CentralityMeasureType lookupType(String displayName) {
		for (CentralityMeasureType type : CentralityMeasureType.values()) {
            if (displayName.equals(type.getDisplayName())) {
                return type;
            }
        }
        throw new InvalidParameterException();
	}
	
	/**
	 * States whether the corresponding creation method class is actually a CentralityAlgorithm.
	 * @return TRUE if the class is a CentralityAlgorithm, otherwise FALSE.
	 */
	public boolean correspondsAlgorithm() {
		if(CentralityAlgorithm.class.isAssignableFrom(this.getCreationMethodClass())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Returns the correct name of the centrality measure.
	 */
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
}
