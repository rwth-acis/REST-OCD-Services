package i5.las2peer.services.ocd.adapters.coverOutput;

import java.security.InvalidParameterException;
import java.util.Locale;

import i5.las2peer.services.ocd.utils.EnumDisplayNames;

/**
 * CoverOutputAdapter registry.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum CoverOutputFormat implements EnumDisplayNames{

	/*
	 * Each enum constant is instantiated with a corresponding CoverOutputAdapter class object and a UNIQUE id.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	/**
	 * Format corresponding to the MetaXmlCoverOutputAdapter.
	 */
	META_XML ("Meta XML", MetaXmlCoverOutputAdapter.class, 0),
	/**
	 * Format corresponding to the DefaultXmlCoverOutputAdapter.
	 */
	DEFAULT_XML ("Default XML", DefaultXmlCoverOutputAdapter.class, 1),
	/**
	 * Format corresponding to the LabeledMembershipMatrixCoverOutputAdapter.
	 */
	LABELED_MEMBERSHIP_MATRIX ("Labeled Membership Matrix", LabeledMembershipMatrixCoverOutputAdapter.class, 2);
	
	/**
	 * The adapter class corresponding to the format.
	 */
	private final Class<? extends CoverOutputAdapter> adapterClass;
	
	/**
	 * Reserved for persistence or other purposes.
	 */
	private final int id;
	
	/**
	 * A display name for web frontends and more
	 */
	private final String displayName;
	
	/**
	 * Creates a new instance.
	 * @param adapterClass Defines the adapterClass attribute.
	 * @param id Defines the id attribute.
	 */
	private CoverOutputFormat(String displayName, Class<? extends CoverOutputAdapter> adapterClass, int id) {
		this.displayName = displayName;
		this.adapterClass = adapterClass;
		this.id = id;
	}
	
	/**
	 * Returns the CoverOutputAdapter subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	protected final Class<? extends CoverOutputAdapter> getAdapterClass() {
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
	public static CoverOutputFormat lookupFormat(int id) {
        for (CoverOutputFormat format : CoverOutputFormat.values()) {
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
