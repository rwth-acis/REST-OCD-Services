package i5.las2peer.services.ocd.centrality.data;

import java.security.InvalidParameterException;
import java.util.Locale;

public enum CentralityCreationType implements CentralityType {
	
	UNDEFINED(0),
	
	/**
	 * Type corresponding to centrality measures
	 */
	CENTRALITY_MEASURE(1),
	
	/**
	 * Type corresponding to simulations, e.g. of the spreading influence of nodes
	 */
	SIMULATION(2),
	
	/**
	 * Abstract type for ground-truth centrality maps
	 */
	GROUND_TRUTH(3),
	
	/**
	 * Type corresponding to averaging of centrality values
	 */
	AVERAGE(4);
	
	/**
	 * For persistence and other purposes.
	 */
	private final int id;
	
	/**
	 * Creates a new instance.
	 * @param id The creation type id
	 */
	CentralityCreationType(int id) {
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
