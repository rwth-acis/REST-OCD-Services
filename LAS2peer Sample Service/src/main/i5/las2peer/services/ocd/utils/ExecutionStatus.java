package i5.las2peer.services.ocd.utils;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * Represents the execution status of an algorithm, benchmark graph calculation or metric.
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
	
	private final int id;
	
	private ExecutionStatus(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public static ExecutionStatus lookupStatus(int id) {
        for (ExecutionStatus type : ExecutionStatus.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        throw new InvalidParameterException();
	}
	
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
}
