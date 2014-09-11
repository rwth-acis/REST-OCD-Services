package i5.las2peer.services.ocd.adapters.coverInput;

import java.security.InvalidParameterException;
import java.util.Locale;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Representation of Graph Input Formats.
 * @author Sebastian
 *
 */
public enum CoverInputFormat {

	COMMUNITY_MEMBERS_LISTS (0),
	NODE_COMMUNITY_LISTS (1),
	LABELED_MEMBERSHIP_MATRIX (2);
	
	private final int id;
	
	private CoverInputFormat(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public static CoverInputFormat lookupFormat(int id) {
        for (CoverInputFormat format : CoverInputFormat.values()) {
            if (id == format.getId()) {
                return format;
            }
        }
        throw new InvalidParameterException();
	}
	
	public CoverInputAdapter getAdapterInstance() {
		switch (this) {
			case COMMUNITY_MEMBERS_LISTS:
				return new CommunityMemberListsCoverInputAdapter();
			case NODE_COMMUNITY_LISTS:
				return new NodeCommunityListsCoverInputAdapter();
			case LABELED_MEMBERSHIP_MATRIX:
				return new LabeledMembershipMatrixCoverInputAdapter();
			default:
				throw new NotImplementedException("Cover input adapter not registered.");
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
