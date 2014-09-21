package i5.las2peer.services.ocd.utils;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * Defines the execution status of an algorithm, benchmark graph calculation or metric.
 * @author Sebastian
 *
 */
public enum ExecutionStatus {
	/**
	 * Is waiting for execution.
	 */
	WAITING(0),
	/**
	 * Is being executed.
	 */
	RUNNING(1),
	/**
	 * Has successfully completed its execution.
	 */
	COMPLETED(2),
	/**
	 * Was stopped due to an exception or error.
	 */
	ERROR(3);
	
	/**
	 * The status id, used for persistence and other purposes.
	 */
	private final int id;
	
	/**
	 * Creates a new instance.
	 * @param id The status id.
	 */
	private ExecutionStatus(int id) {
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
	 * Returns the status corresponding to an id.
	 * @param id The id.
	 * @return The status.
	 */
	public static ExecutionStatus lookupStatus(int id) {
        for (ExecutionStatus type : ExecutionStatus.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        throw new InvalidParameterException();
	}
	
	/**
	 * Returns the name of the status written in lower case letters and with any underscores replaced by space characters.
	 */
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
}
