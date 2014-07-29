package i5.las2peer.services.servicePackage.metrics;

import java.util.Locale;

public enum MetricType {

	UNDEFINED (0),
	EXTENDED_MODULARITY_METRIC (1),
	EXTENDED_NORMALIZED_MUTUAL_INFORMATION (2),
	EXECUTION_TIME (3);
	
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

	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
	
}
