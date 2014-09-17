package i5.las2peer.services.ocd.adapters.coverInput;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * CoverInputAdapter registry.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum CoverInputFormat {

	/*
	 * Each enum constant is instantiated with a corresponding CoverInputAdapter class object and a UNIQUE id.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	COMMUNITY_MEMBERS_LISTS (CommunityMemberListsCoverInputAdapter.class, 0),
	NODE_COMMUNITY_LISTS (NodeCommunityListsCoverInputAdapter.class, 1),
	LABELED_MEMBERSHIP_MATRIX (LabeledMembershipMatrixCoverInputAdapter.class, 2);
	
	private final int id;
	
	private final Class<? extends CoverInputAdapter> adapterClass;
	
	private CoverInputFormat(Class<? extends CoverInputAdapter> adapterClass, int id) {
		this.id = id;
		this.adapterClass = adapterClass;
	}
	
	protected Class<? extends CoverInputAdapter> getAdapterClass() {
		return this.adapterClass;
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

	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
	
}
