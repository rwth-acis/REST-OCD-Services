package i5.las2peer.services.servicePackage.graph;

import java.util.Locale;

/**
 * Used to indicate the characteristics of a graph.
 * @author Sebastian
 *
 */
public enum GraphType {
	/**
	 * Indicates that a graph is weighted.
	 * I.e. edge weights may assume values other than one.
	 */
	WEIGHTED(0),
	/**
	 * Indicates that a graph is directed.
	 * I.e. there may be edges without a reverse edge leading the opposite way.
	 */
	DIRECTED(1),
	/**
	 * Indicates that a graph has negative edge weights.
	 */
	NEGATIVE_WEIGHTS(2),
	/**
	 * Indicates that a graph has edge weights equal to 0.
	 */
	ZERO_WEIGHTS(3),
	/**
	 * Indicates that a graph has self loops.
	 * I.e. there may be edges with identical source and target node.
	 */
	SELF_LOOPS(4);
	
	private int id;
	
	private GraphType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public static GraphType lookupType(int id) {
        for (GraphType type : GraphType.values()) {
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
