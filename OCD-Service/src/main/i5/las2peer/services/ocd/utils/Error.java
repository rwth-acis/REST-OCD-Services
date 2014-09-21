package i5.las2peer.services.ocd.utils;

import java.util.Locale;

/**
 * Defines service interface error types and ids.
 * @author Sebastian
 *
 */
public enum Error {
	
	/**
	 * Unspecified error. ID is 0.
	 */
	UNDEFINED(0),
	/**
	 * Invalid input parameter. ID is 1.
	 */
	PARAMETER_INVALID(1),
	/**
	 * Internal error. ID is 2.
	 */
	INTERNAL(2);
	
	/**
	 * The error id.
	 */
	private final int id;
	
	/**
	 * Creates a new instance.
	 * @param id The error id.
	 */
	private Error(int id) {
		this.id = id;
	}
	
	/**
	 * Returns the error id.
	 * @return The id.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns the name of the error written in lower case letters and with any underscores replaced by space characters.
	 */
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
	
}
