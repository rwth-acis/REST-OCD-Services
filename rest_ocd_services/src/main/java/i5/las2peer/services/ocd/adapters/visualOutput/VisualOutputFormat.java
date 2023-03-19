package i5.las2peer.services.ocd.adapters.visualOutput;

import java.security.InvalidParameterException;
import java.util.Locale;

import i5.las2peer.services.ocd.utils.EnumDisplayNames;

/**
 * VisualOutputAdapter registry.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum VisualOutputFormat implements EnumDisplayNames {

	/*
	 * Each enum constant is instantiated with a corresponding VisualOutputAdapter class object and a UNIQUE id.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	/**
	 * Format corresponding to the SvgVisualOutputAdapter.
	 */
	SVG ("SVG", SvgVisualOutputAdapter.class, 0),
	
	/**
	 * Format corresponding to the JsonVisualOutputAdapter.
	 */
	JSON ("Json", JsonVisualOutputAdapter.class, 1);
	
	/**
	 * The adapter class corresponding to the format.
	 */
	private final Class<? extends VisualOutputAdapter> adapterClass;
	
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
	private VisualOutputFormat(String displayName, Class<? extends VisualOutputAdapter> adapterClass, int id) {
		this.displayName = displayName;
		this.adapterClass = adapterClass;
		this.id = id;
	}
	
	/**
	 * Returns the VisualOutputAdapter subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	protected Class<? extends VisualOutputAdapter> getAdapterClass() {
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
	public static VisualOutputFormat lookupFormat(int id) {
        for (VisualOutputFormat format : VisualOutputFormat.values()) {
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

