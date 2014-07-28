package i5.las2peer.services.servicePackage.graph;

/**
 * Used to indicate the characteristics of a graph.
 * @author Sebastian
 *
 */
public enum GraphType {
	/**
	 * Indicates that the graph is weighted.
	 * I.e. edge weights may assume values other than one.
	 */
	WEIGHTED,
	/**
	 * Indicates that the graph is directed.
	 * I.e. there may be edges without a reverse edge leading the opposite way.
	 */
	DIRECTED,
	/**
	 * Indicates that the graph allows negative edge weights.
	 * I.e. there may be edges with an edge weight smaller than zero.
	 */
	NEGATIVE_EDGE_WEIGHTS
}
