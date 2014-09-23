package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.benchmarks.GroundTruthBenchmark;
import i5.las2peer.services.ocd.benchmarks.LfrBenchmark;
import i5.las2peer.services.ocd.benchmarks.NewmanBenchmark;
import i5.las2peer.services.ocd.benchmarks.OcdBenchmark;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * Graph creation method registry.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum GraphCreationType {

	/*
	 * Each enum constant is instantiated with a corresponding OcdBenchmark class object and a UNIQUE id.
	 * Abstract types that do not correspond to any benchmark are instantiated with the GraphCreationMethod interface itself.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	/**
	 * Abstract type usable e.g. for importing covers that were calculated externally by other benchmarks.
	 * Cannot be used for benchmark instantiation.
	 */
	UNDEFINED (GraphCreationMethod.class, 0),
	/**
	 * Abstract type mainly intended for importing real world covers.
	 * Cannot be used for benchmark instantiation.
	 */
	REAL_WORLD (GraphCreationMethod.class, 1),
	/**
	 * Type corresponding to the NewmanBenchmark.
	 */
	NEWMAN (NewmanBenchmark.class, 2),
	/**
	 * Type corresponding to the LfrBenchmark.
	 */
	LFR (LfrBenchmark.class, 3);
	
	/**
	 * For persistence and other purposes.
	 */
	private final int id;
	
	/**
	 * The class corresponding to the type.
	 * Abstract types correspond to the GraphCreationMethod interface itself.
	 */
	private final Class<? extends GraphCreationMethod> creationMethodClass;
	
	/**
	 * Creates a new instance.
	 * @param creationMethodClass Defines the creationMethodClass attribute.
	 * @param id Defines the id attribute.
	 */
	private GraphCreationType(Class<? extends GraphCreationMethod> creationMethodClass, int id) {
		this.creationMethodClass = creationMethodClass;
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
	 * Returns the GraphCreationMethod subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	public Class<? extends GraphCreationMethod> getCreationMethodClass() {
		return this.creationMethodClass;
	}
	
	/**
	 * Returns the type corresponding to an id.
	 * @param id The id.
	 * @return The corresponding type.
	 */
	public static GraphCreationType lookupType(int id) {
        for (GraphCreationType type : GraphCreationType.values()) {
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
	
	/**
	 * States whether the corresponding GraphCreationMethod class is a ground truth benchmark.
	 * @return TRUE if the class is a ground truth benchmark, otherwise FALSE.
	 */
	public boolean correspondsGroundTruthBenchmark() {
		if(GroundTruthBenchmark.class.isAssignableFrom(this.getCreationMethodClass())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * States whether the corresponding GraphCreationMethod class is a benchmark.
	 * @return TRUE if the class is a ground truth benchmark, otherwise FALSE.
	 */
	public boolean correspondsBenchmark() {
		if(OcdBenchmark.class.isAssignableFrom(this.getCreationMethodClass())) {
			return true;
		}
		else {
			return false;
		}
	}
	
}
