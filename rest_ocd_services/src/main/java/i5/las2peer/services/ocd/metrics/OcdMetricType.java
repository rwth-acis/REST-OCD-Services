package i5.las2peer.services.ocd.metrics;

import java.security.InvalidParameterException;
import java.util.Locale;

import i5.las2peer.services.ocd.utils.EnumDisplayNames;

/**
 * OcdMetric registry.
 * Used for factory instantiation, persistence or other context.
 */
public enum OcdMetricType implements EnumDisplayNames{

	/*
	 * Each enum constant is instantiated with a corresponding OcdMetric class object and a UNIQUE id.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	/**
	 * Abstract type usable e.g. for metrics calculated externally.
	 * Cannot be used for metric instantiation.
	 */
	UNDEFINED ("Undefined", OcdMetric.class, 0),
	/**
	 * Abstract type for the algorithm execution time.
	 * Cannot be used for metric instantiation.
	 * An execution time metric entry is automatically added to any cover calculated by a framework algorithm.
	 */
	EXECUTION_TIME ("Execution Time", OcdMetric.class, 1),
	/**
	 * Type corresponding to the ExtendedModularityMetric, a statistical measure.
	 */
	EXTENDED_MODULARITY ("Extended Modularity", ExtendedModularityMetric.class, 2),
	/**
	 * Type corresponding to the ExtendedNormalizedMutualInformationMetric, a knowledge-driven measure.
	 */
	EXTENDED_NORMALIZED_MUTUAL_INFORMATION ("Extended Normalized Mutual Information", ExtendedNormalizedMutualInformationMetric.class, 3),
	/**
	 * Type corresponding to the OmegaIndex, a knowledge-driven measure.
	 */
	OMEGA_INDEX ("Omega Index", OmegaIndex.class, 4),
	
	/**
	 * Type corresponding to the Combined Newman Modularity, a statistical measure.
	 */
	COMBINED_MODULARITY ("Combined Newman Modularity", NewmanModularityCombined.class, 5),
	
	/**
	 * Type corresponding to the ExtendedModularity using co memberships, a statistical measure.
	 */
	CO_MEMBERSHIP_MODULARITY ("Extended Modularity with Co-Memberships", ExtendedModularityMetricCoMembership.class, 6),
	
	/**
	 * Type corresponding to Newmans Modularity, a statistical measure.
	 */
	NEWMAN_MODULARITY ("Newman Modularity", ModularityMetric.class, 7),
		/**
	 * Type corresponding to Muliplex Modularity, a statistical measure.
	 */
	MULTIPLEX_MODULARITY ("Multiplex Modularity", MultiplexModularityMetric.class, 8);

	
	/**
	 * For persistence and other purposes.
	 */
	private final int id;
	
	/**
	 * A display name for web frontends and more
	 */
	private final String displayName;
	
	/**
	 * The class corresponding to the type.
	 * Abstract types correspond to the OcdMetric interface itself.
	 */
	private final Class<? extends OcdMetric> metricClass;
	
	/**
	 * Creates a new instance.
	 * @param metricClass Defines the metricClass attribute.
	 * @param id Defines the id attribute.
	 */
	private OcdMetricType(String displayName, Class<? extends OcdMetric> metricClass, int id) {
		this.displayName = displayName;
		this.metricClass = metricClass;
		this.id = id;
	}
	
	/**
	 * Returns the OcdMetric subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	protected Class<? extends OcdMetric> getMetricClass() {
		return this.metricClass;
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
	public static OcdMetricType lookupType(int id) {
        for (OcdMetricType type : OcdMetricType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        throw new InvalidParameterException();
	}
	
	/**
	 * Returns the type corresponding to an OcdMetric class.
	 * @param metricClass The class.
	 * @return The corresponding type.
	 */
	public static OcdMetricType lookupType(Class<? extends OcdMetric> metricClass) {
        for (OcdMetricType type : OcdMetricType.values()) {
            if (metricClass == type.getMetricClass()) {
                return type;
            }
        }
        throw new InvalidParameterException();
	}
	
	/**
	 * States whether the corresponding OcdMetric class is a statistical measure.
	 * @return TRUE if the class is a statistical measure, otherwise FALSE.
	 */
	public boolean correspondsStatisticalMeasure() {
		if(StatisticalMeasure.class.isAssignableFrom(this.getMetricClass())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * States whether the corresponding OcdMetric class is a knowledge-driven measure.
	 * @return TRUE if the class is a knowledge-driven measure, otherwise FALSE.
	 */
	public boolean correspondsKnowledgeDrivenMeasure() {
		if(KnowledgeDrivenMeasure.class.isAssignableFrom(this.getMetricClass())) {
			return true;
		}
		else {
			return false;
		}
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
