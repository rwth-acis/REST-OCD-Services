package i5.las2peer.services.servicePackage.metrics;

import java.util.Locale;

public enum Metric {
	ExtendedModularityMetric;
	
	@Override
	public String toString() {
		String name = name();
		name.replace('_', ' ');
		name.toLowerCase(Locale.ROOT);
		return name;
	}
}
