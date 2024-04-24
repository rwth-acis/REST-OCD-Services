package i5.las2peer.services.ocd.graphs;

import java.util.Locale;

import i5.las2peer.services.ocd.utils.EnumDisplayNames;

/**
 * Used to indicate the characteristics of a graph.
 * @author Sebastian
 *
 */
public enum GraphType implements EnumDisplayNames {
	/*
	 * Each enum constant is instantiated with a UNIQUE id.
	 */
	/**
	 * Indicates that a graph is weighted.
	 * I.e. edge weights may assume values other than one.
	 */
	WEIGHTED("Weighted", 0),
	/**
	 * Indicates that a graph is directed.
	 * I.e. there may be edges without a reverse edge leading the opposite way
	 * or with a reverse edge of different weight.
	 */
	DIRECTED("Directed", 1),
	/**
	 * Indicates that a graph has negative edge weights.
	 */
	NEGATIVE_WEIGHTS("Negative Weights", 2),
	/**
	 * Indicates that a graph has edge weights equal to 0.
	 */
	ZERO_WEIGHTS("Zero Weights", 3),
	/**
	 * Indicates that a graph has self loops.
	 * I.e. there may be edges with identical source and target node.
	 */
	SELF_LOOPS("Self Loops", 4),
	
	/**
	 * Indicates that a graph contains the content attribute for each node.
	 */	
	CONTENT_UNLINKED("Content Unlinked", 5),
	
	/**
	 * Indicates that a graph contains the content attribute for each node and
	 *  that there can be identified a sender and receiver for link creation.
	 */
	CONTENT_LINKED("Content Linked", 6),

	/**
	 *  Indicates that a graph is dynamic in nature. It's edges
	 *  have timestamps and an action.
	 */
	DYNAMIC("Dynamic", 7);

	/**
	 * For persistence and other purposes.
	 */
	private int id;
	
	/**
	 * A display name for web frontends and more
	 */
	private final String displayName;
	
	/**
	 * Creates a new instance.
	 * @param id Defines the id attribute.
	 */
	private GraphType(String displayName, int id) {
		this.displayName = displayName;
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
	 * Returns the display name of the type.
	 * @return The name.
	 */
	public String getDisplayName() {
		return displayName;
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
