package i5.las2peer.services.ocd.cooperation.simulation.termination;

import java.security.InvalidParameterException;


/**
 * This Enum lists all available Break Condition
 */
public enum ConditionType {

	UNKNOWN("Unknown", Condition.class, 0),

	FIXED_ITERATIONS("Fixed Iterations", FixedIterationsCondition.class, 1),

	STATIONARY_STATE("Stationary State", StationaryStateCondition.class, 2);

	/**
	 * A String representation of the dynamic
	 */
	public final String humanread;

	/**
	 * The class that implements the dynamic
	 */
	public final Class<? extends Condition> enumClass;

	/**
	 * Integer representation of the ConditionType
	 */
	public final int id;

	ConditionType(String humanread, Class<? extends Condition> enumClass, int id) {
		this.humanread = humanread;
		this.id = id;
		this.enumClass = enumClass;
	}

	/**
	 * @return the integer representation
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * @return the human readable string representation
	 */
	public String humanRead() {
		return this.humanread;
	}

	/**
	 * @param typeString
	 *            condition type as string
	 * @return true if condition type exists
	 */
	public static boolean TypeExists(String typeString) {

		for (ConditionType type : ConditionType.values()) {
			if (typeString.equalsIgnoreCase(type.name()) || typeString.equalsIgnoreCase(type.humanRead())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the Condition subclass corresponding to the type.
	 * 
	 * @return The corresponding class.
	 */
	protected final Class<? extends Condition> getEnumClass() {
		return this.enumClass;
	}

	/**
	 * @param typeString
	 *            condition type as string
	 * @return the ConditionType
	 */
	public static ConditionType fromString(String typeString) {

		for (ConditionType type : ConditionType.values()) {
			if (typeString.equalsIgnoreCase(type.name()) || typeString.equalsIgnoreCase(type.humanRead())
			) {
				return type;
			}
		}

		throw new IllegalArgumentException("type not known");

	}

	/**
	 * Returns the ConditionType of its corresponding class.
	 * 
	 * @param enumClass the enum class
	 * @return the enumType
	 */
	public static ConditionType getType(Class<? extends Condition> enumClass) {
		for (ConditionType type : ConditionType.values()) {
			if (enumClass == type.getEnumClass()) {
				return type;
			}
		}
		throw new InvalidParameterException();
	}

	public static ConditionType[] getValues() {
		return ConditionType.values();
	}

}
