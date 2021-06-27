package i5.las2peer.services.ocd.adapters.centralityInput;

import java.security.InvalidParameterException;
import java.util.Locale;

import i5.las2peer.services.ocd.utils.EnumDisplayNames;

public enum CentralityInputFormat implements EnumDisplayNames {

	/*
	 * Each enum constant is instantiated with a corresponding CentralityInputAdapter class object and a UNIQUE id.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	/**
	 * Format corresponding the NodeValueInputAdapter.
	 */
	NODE_VALUE_LIST ("Node Value List", NodeValueListInputAdapter.class, 0);
	
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
	private final Class<? extends CentralityInputAdapter> adapterClass;
	
	/**
	 * Creates a new instance.
	 * @param adapterClass Defines the adapterClass attribute.
	 * @param id Defines the id attribute.
	 */
	private CentralityInputFormat(String displayName, Class<? extends CentralityInputAdapter> adapterClass, int id) {
		this.displayName = displayName;
		this.id = id;
		this.adapterClass = adapterClass;
	}
	
	/**
	 * Returns the CentralityInputAdapter subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	protected Class<? extends CentralityInputAdapter> getAdapterClass() {
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
	public static CentralityInputFormat lookupFormat(int id) {
        for (CentralityInputFormat format : CentralityInputFormat.values()) {
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
