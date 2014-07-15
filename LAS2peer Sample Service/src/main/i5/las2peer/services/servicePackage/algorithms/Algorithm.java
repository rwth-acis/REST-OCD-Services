package i5.las2peer.services.servicePackage.algorithms;

import java.util.Locale;

/**
 * An enum representation for overlapping community detection algorithms.
 * @author Sebastian
 *
 */
public enum Algorithm {
	UNDEFINED,
	RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM,
	SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM,
	SSK_ALGORITHM,
	LINK_COMMUNITIES_ALGORITHM,
	CLIZZ_ALGORITHM;
	
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
}
