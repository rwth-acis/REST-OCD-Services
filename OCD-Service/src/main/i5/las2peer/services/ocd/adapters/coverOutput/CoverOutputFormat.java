package i5.las2peer.services.ocd.adapters.coverOutput;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * CoverOutputAdapter registry.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum CoverOutputFormat {

	/*
	 * Each enum constant is instantiated with a corresponding CoverOutputAdapter class object and a UNIQUE id.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	/**
	 * Format corresponding to the MetaXmlCoverOutputAdapter.
	 */
	META_XML (MetaXmlCoverOutputAdapter.class, 0),
	/**
	 * Format corresponding to the DefaultXmlCoverOutputAdapter.
	 */
	DEFAULT_XML (DefaultXmlCoverOutputAdapter.class, 1),
	/**
	 * Format corresponding to the LabeledMembershipMatrixCoverOutputAdapter.
	 */
	LABELED_MEMBERSHIP_MATRIX (LabeledMembershipMatrixCoverOutputAdapter.class, 2);
	
	/**
	 * The adapter class corresponding to the format.
	 */
	private final Class<? extends CoverOutputAdapter> adapterClass;
	
	/**
	 * Reserved for persistence or other purposes.
	 */
	private final int id;
	
	/**
	 * Creates a new instance.
	 * @param adapterClass Defines the adapterClass attribute.
	 * @param id Defines the id attribute.
	 */
	private CoverOutputFormat(Class<? extends CoverOutputAdapter> adapterClass, int id) {
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
