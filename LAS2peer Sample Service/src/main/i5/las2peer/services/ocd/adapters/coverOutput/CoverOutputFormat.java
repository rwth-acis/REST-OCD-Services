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
	META_XML (MetaXmlCoverOutputAdapter.class, 0),
	DEFAULT_XML (DefaultXmlCoverOutputAdapter.class, 1),
	LABELED_MEMBERSHIP_MATRIX (LabeledMembershipMatrixCoverOutputAdapter.class, 2);
	
	private final Class<? extends CoverOutputAdapter> adapterClass;
	
	private final int id;
	
	private CoverOutputFormat(Class<? extends CoverOutputAdapter> adapterClass, int id) {
		this.adapterClass = adapterClass;
		this.id = id;
	}
	
	protected final Class<? extends CoverOutputAdapter> getAdapterClass() {
		return this.adapterClass;
	}
	
	public int getId() {
		return id;
	}
	
	public static CoverOutputFormat lookupFormat(int id) {
        for (CoverOutputFormat format : CoverOutputFormat.values()) {
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
