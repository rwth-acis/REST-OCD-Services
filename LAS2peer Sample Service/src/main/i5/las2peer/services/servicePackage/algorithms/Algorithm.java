package i5.las2peer.services.servicePackage.algorithms;

import java.util.Locale;

public enum Algorithm {
	UNDEFINED,
	RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM,
	SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM,
	SSK_ALGORITHM,
	LINK_COMMUNITIES_ALGORITHM;
	
	@Override
	public String toString() {
		String name = name();
		name.replace('_', ' ');
		name.toLowerCase(Locale.ROOT);
		return name;
	}
}
