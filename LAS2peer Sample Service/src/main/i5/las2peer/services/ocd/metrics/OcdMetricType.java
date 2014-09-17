package i5.las2peer.services.ocd.metrics;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * OcdMetric registry.
 * Used for factory instantiation, persistence or other context.
 */
public enum OcdMetricType {

	/*
	 * Each enum constant is instantiated with a corresponding OcdMetric class object and a UNIQUE id.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	UNDEFINED (OcdMetric.class, 0),
	EXECUTION_TIME (OcdMetric.class, 1),
	EXTENDED_MODULARITY (ExtendedModularityMetric.class, 2),
	EXTENDED_NORMALIZED_MUTUAL_INFORMATION (ExtendedNormalizedMutualInformationMetric.class, 3),
	OMEGA_INDEX (OmegaIndex.class, 4);
	
	private int id;
	Class<? extends OcdMetric> metricClass;
	
	private OcdMetricType(Class<? extends OcdMetric> metricClass, int id) {
		this.metricClass = metricClass;
		this.id = id;
	}
	
	protected Class<? extends OcdMetric> getMetricClass() {
		return this.metricClass;
	}
	
	public int getId() {
		return id;
	}
	
	public static OcdMetricType lookupType(int id) {
        for (OcdMetricType type : OcdMetricType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        throw new InvalidParameterException();
	}
	
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
	
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
	
}
