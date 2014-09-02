package i5.las2peer.services.ocd.metrics;

import java.security.InvalidParameterException;
import java.util.Locale;

import org.apache.commons.lang3.NotImplementedException;

public enum MetricType {

	UNDEFINED (0),
	EXECUTION_TIME (1),
	EXTENDED_MODULARITY (2),
	EXTENDED_NORMALIZED_MUTUAL_INFORMATION (3),
	OMEGA_INDEX (4);
	
	private int id;
	
	private MetricType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public static MetricType lookupType(int id) {
        for (MetricType type : MetricType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        throw new InvalidParameterException();
	}
	
	public boolean isStatisticalMeasure() {
		switch (this) {
		case EXTENDED_MODULARITY:
			return true;
		default:
			return false;
		}
	}
	
	public boolean isKnowledgeDrivenMeasure() {
		switch (this) {
		case EXTENDED_NORMALIZED_MUTUAL_INFORMATION:
			return true;
		case OMEGA_INDEX:
			return true;
		default:
			return false;
		}
	}
	
	public KnowledgeDrivenMeasure getKnowledgeDrivenMeasureInstance() {
		switch (this) {
		case EXTENDED_NORMALIZED_MUTUAL_INFORMATION:
			return new ExtendedNormalizedMutualInformation();
		case OMEGA_INDEX:
			return new OmegaIndex();
		default:
			throw new NotImplementedException("Metric not registered.");
		}
	}
	
	public StatisticalMeasure getStatisticalMeasureInstance() {
		switch (this) {
		case EXTENDED_MODULARITY:
			return new ExtendedModularity();
		default:
			return null;
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
