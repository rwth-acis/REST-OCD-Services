package i5.las2peer.services.ocd.centrality.data;

import java.security.InvalidParameterException;
import java.util.Locale;

import i5.las2peer.services.ocd.utils.EnumDisplayNames;

public enum CentralityCreationType implements CentralityType, EnumDisplayNames {
	
	UNDEFINED("Undefined", 0),
	
	/**
	 * Type corresponding to centrality measures
	 */
	CENTRALITY_MEASURE("Centrality Measure", 1),
	
	/**
	 * Type corresponding to simulations, e.g. of the spreading influence of nodes
	 */
	SIMULATION("Simulation", 2),
	
	/**
	 * Abstract type for ground-truth centrality maps
	 */
	GROUND_TRUTH("Ground Truth", 3),
	
	/**
	 * Type corresponding to averaging of centrality values
	 */
	AVERAGE("Average", 4);
	
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
	 * @param id The creation type id
	 */
	CentralityCreationType(String displayName, int id) {
		this.displayName = displayName;
		this.id = id;
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
	public static CentralityCreationType lookupType(int id) {
        for (CentralityCreationType type : CentralityCreationType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        throw new InvalidParameterException();
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
