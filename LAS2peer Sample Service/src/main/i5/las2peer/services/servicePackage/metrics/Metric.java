package i5.las2peer.services.servicePackage.metrics;

import java.util.Locale;

public enum Metric {
	EXTENDED_MODULARITY_METRIC,
	EXTENDED_NORMALIZED_MUTUAL_INFORMATION;
	
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
}
