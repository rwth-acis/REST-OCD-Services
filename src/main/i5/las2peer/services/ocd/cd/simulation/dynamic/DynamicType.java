package i5.las2peer.services.ocd.cd.simulation.dynamic;

import java.security.InvalidParameterException;


/**
 * This Enum lists all available Dynamics. Have to be extended after implementing a new
 * dynamic subclass. The string value is used as end user representation
 */
public enum DynamicType {

	UNKNOWN("Unknown", "UK", Dynamic.class, 0, "Unknown"),

	REPLICATOR("Replicator", "REP", Replicator.class, 1, ""),

	UNCONDITIONAL_IMITATION("Imitation", "IM", UnconditionalImitation.class, 2, ""),

	MORAN("Moran", "MOR", Moran.class, 3, ""),

	WS_LS("Win-Stay Lose-Shift", "WSLS", WinStayLoseShift.class, 4, "");

	/**
	 * A String representation of the dynamic
	 */
	public final String humanread;

	/**
	 * Shortcut representation of the dynamic
	 */
	public final String shortcut;

	/**
	 * The class that implements the dynamic
	 */
	public final Class<? extends Dynamic> dynamicClass;

	/**
	 * Integer representation of the DynamicType
	 */
	public final int id;

	/**
	 * the description of the dynamic
	 */
	public final String description;

	DynamicType(String humanread, String shortcut, Class<? extends Dynamic> dynamicClass, int id, String description) {
		this.humanread = humanread;
		this.shortcut = shortcut;
		this.id = id;
		this.dynamicClass = dynamicClass;
		this.description = description;
	}

	/**
	 * @return the integer representation
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * @return the shortcut representation
	 */
	public String shortcut() {
		return this.shortcut;
	}

	/**
	 * @return the human readable string representation
	 */
	public String humanRead() {
		return this.humanread;
	}

	/**
	 * @return the description of the dynamic
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @param typeString
	 *            dynamic type as string
	 * @return true if dynamic type exists
	 */
	public static boolean TypeExists(String typeString) {

		for (DynamicType type : DynamicType.values()) {
			if (typeString.equalsIgnoreCase(type.name()) || typeString.equalsIgnoreCase(type.humanRead())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the Dynamic subclass corresponding to the type.
	 * 
	 * @return The corresponding class.
	 */
	protected final Class<? extends Dynamic> getDynamicClass() {
		return this.dynamicClass;
	}

	/**
	 * @param typeString
	 *            dynamic type as string
	 * @return the DynamicType
	 */
	public static DynamicType fromString(String typeString) {

		for (DynamicType type : DynamicType.values()) {
			if (typeString.equalsIgnoreCase(type.name()) || typeString.equalsIgnoreCase(type.humanRead())
					|| typeString.equalsIgnoreCase(type.shortcut())) {
				return type;
			}
		}
		return DynamicType.UNKNOWN;
	}

	/**
	 * Returns the DynamicType of its corresponding class.
	 * 
	 * @param dynamicClass
	 * @return the dynamicType
	 */
	public static DynamicType getType(Class<? extends Dynamic> dynamicClass) {
		for (DynamicType type : DynamicType.values()) {
			if (dynamicClass == type.getDynamicClass()) {
				return type;
			}
		}
		throw new InvalidParameterException();
	}

	public static DynamicType[] getValues() {
		return DynamicType.values();
	}

}
