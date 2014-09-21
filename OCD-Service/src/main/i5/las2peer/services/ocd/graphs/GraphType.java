package i5.las2peer.services.ocd.graphs;

import java.util.Locale;

/**
 * Used to indicate the characteristics of a graph.
 * @author Sebastian
 *
 */
public enum GraphType {
	/*
	 * Each enum constant is instantiated with a UNIQUE id.
	 */
	/**
	 * Indicates that a graph is weighted.
	 * I.e. edge weights may assume values other than one.
	 */
	WEIGHTED(0),
	/**
	 * Indicates that a graph is directed.
	 * I.e. there may be edges without a reverse edge leading the opposite way
	 * or with a reverse edge of different weight.
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
	
	/**
	 * For persistence and other purposes.
	 */
	private int id;
	
	/**
	 * Creates a new instance.
	 * @param id Defines the id attribute.
	 */
	private GraphType(int id) {
		this.id = id;
	}
	
	/**
	 * Getter for the id.
	 * @return The id.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns the type corresponding to an id.
	 * @param id The id.
	 * @return The type.
	 */
	public static GraphType lookupType(int id) {
        for (GraphType type : GraphType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        return null;
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
