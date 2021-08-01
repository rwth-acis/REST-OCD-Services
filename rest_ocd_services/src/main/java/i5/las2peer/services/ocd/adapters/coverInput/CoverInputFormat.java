package i5.las2peer.services.ocd.adapters.coverInput;

import java.security.InvalidParameterException;
import java.util.Locale;

import i5.las2peer.services.ocd.utils.EnumDisplayNames;

/**
 * CoverInputAdapter registry.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum CoverInputFormat implements EnumDisplayNames {

	/*
	 * Each enum constant is instantiated with a corresponding CoverInputAdapter class object and a UNIQUE id.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	/**
	 * Format corresponding the CommunityMemberListsCoverInputAdapter.
	 */
	COMMUNITY_MEMBERS_LISTS ("Community Member Lists", CommunityMemberListsCoverInputAdapter.class, 0),
	/**
	 * Format corresponding the NodeCommunityListsCoverInputAdapter.
	 */
	NODE_COMMUNITY_LISTS ("Node Community Lists", NodeCommunityListsCoverInputAdapter.class, 1),
	/**
	 * Format corresponding the LabeledMembershipMatrixCoverInputAdapter.
	 */
	LABELED_MEMBERSHIP_MATRIX ("Labeled Membership Matrix", LabeledMembershipMatrixCoverInputAdapter.class, 2);
	
	/**
	 * Reserved for persistence or other purposes.
	 */
	private final int id;
	
	/**
	 * A display name for web frontends and more
	 */
	private final String displayName;
	
	/**
	 * The adapter class corresponding to the format.
	 */
	private final Class<? extends CoverInputAdapter> adapterClass;
	
	/**
	 * Creates a new instance.
	 * @param adapterClass Defines the adapterClass attribute.
	 * @param id Defines the id attribute.
	 */
	private CoverInputFormat(String displayName, Class<? extends CoverInputAdapter> adapterClass, int id) {
		this.displayName = displayName;
		this.id = id;
		this.adapterClass = adapterClass;
	}
	
	/**
	 * Returns the CoverInputAdapter subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	protected Class<? extends CoverInputAdapter> getAdapterClass() {
		return this.adapterClass;
	}
	
	/**
	 * Returns the unique id of the format.
	 * @return The id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the display name of the type.
	 * @return The name.
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * Returns the format corresponding to an id.
	 * @param id The id.
	 * @return The corresponding format.
	 */
	public static CoverInputFormat lookupFormat(int id) {
        for (CoverInputFormat format : CoverInputFormat.values()) {
            if (id == format.getId()) {
                return format;
            }
        }
        throw new InvalidParameterException();
	}

	/**
	 * Returns the name of the format written in lower case letters and with any underscores replaced by space characters.
	 */
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
	
}
