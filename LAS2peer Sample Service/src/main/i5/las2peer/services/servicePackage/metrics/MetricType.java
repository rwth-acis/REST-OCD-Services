package i5.las2peer.services.servicePackage.metrics;

import java.util.Locale;

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
        return null;
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
			return null;
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
