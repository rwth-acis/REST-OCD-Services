package i5.las2peer.services.ocd.utils;

import java.util.Locale;

/**
 * Defines service interface error types and ids.
 * @author Sebastian
 *
 */
public enum Error {
	
	UNDEFINED(0),
	PARAMETER_INVALID(1),
	INTERNAL(2);
	
	private final int id;
	
	private Error(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
	
}
