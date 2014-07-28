package i5.las2peer.services.servicePackage.metrics;

import java.util.Locale;

public enum MetricIdentifier {

	UNDEFINED,
	EXTENDED_MODULARITY_METRIC,
	EXTENDED_NORMALIZED_MUTUAL_INFORMATION,
	EXECUTION_TIME;
	
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
	
}
