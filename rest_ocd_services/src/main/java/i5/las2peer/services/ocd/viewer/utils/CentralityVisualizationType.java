package i5.las2peer.services.ocd.viewer.utils;

import i5.las2peer.services.ocd.adapters.centralityInput.CentralityInputAdapter;
import i5.las2peer.services.ocd.utils.EnumDisplayNames;

/**
 * The different types for visualizing centrality values in a graph.
 * @author Tobias
 *
 */
public enum CentralityVisualizationType implements EnumDisplayNames {
	NODE_SIZE("Node Size", 0),
	COLOR_SATURATION("Color Saturation", 1),
	HEAT_MAP("Heat Map", 2);
	
	/**
	 * Reserved for persistence or other purposes.
	 */
	private final int id;
	
	/**
	 * A display name for web frontends and more
	 */
	private final String displayName;
	
	/**
	 * Creates a new instance.
	 * @param id Defines the id attribute.
	 */
	private CentralityVisualizationType(String displayName, int id) {
		this.displayName = displayName;
		this.id = id;
	}
	
	/**
	 * Returns the unique id of the format.
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
}
