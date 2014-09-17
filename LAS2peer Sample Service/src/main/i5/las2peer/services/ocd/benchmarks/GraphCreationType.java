package i5.las2peer.services.ocd.benchmarks;

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
	 * Any type of benchmark / creation not further defined.
	 */
	UNDEFINED (GraphCreationMethod.class, 0),
	/**
	 * Real world type, i.e. no benchmark.
	 */
	REAL_WORLD (GraphCreationMethod.class, 1),
	/**
	 * Newman benchmark.
	 */
	NEWMAN (NewmanBenchmark.class, 2),
	/**
	 * Lfr benchmark.
	 */
	LFR (LfrBenchmark.class, 3);
	
	private final int id;
	
	private final Class<? extends GraphCreationMethod> creationMethodClass;
	
	private GraphCreationType(Class<? extends GraphCreationMethod> creationMethodClass, int id) {
		this.creationMethodClass = creationMethodClass;
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	protected Class<? extends GraphCreationMethod> getCreationMethodClass() {
		return this.creationMethodClass;
	}
	
	public static GraphCreationType lookupType(int id) {
        for (GraphCreationType type : GraphCreationType.values()) {
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
