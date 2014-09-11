package i5.las2peer.services.ocd.adapters.coverOutput;

import java.security.InvalidParameterException;
import java.util.Locale;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Representation of Graph Input Formats.
 * @author Sebastian
 *
 */
public enum CoverOutputFormat {

	META_XML (0),
	DEFAULT_XML (1),
	LABELED_MEMBERSHIP_MATRIX (2);
	
	private final int id;
	
	private CoverOutputFormat(int id) {
		this.id = id;
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
	
	public CoverOutputAdapter getAdapterInstance() {
		switch (this) {
			case META_XML:
				return new MetaXmlCoverOutputAdapter();
			case DEFAULT_XML:
				return new DefaultXmlCoverOutputAdapter();
			case LABELED_MEMBERSHIP_MATRIX:
				return new LabeledMembershipMatrixCoverOutputAdapter();
			default:
				throw new NotImplementedException("Cover output adapter not registered.");
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
