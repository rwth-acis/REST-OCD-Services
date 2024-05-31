package i5.las2peer.services.ocd.adapters.clcOutput;

import java.security.InvalidParameterException;
import java.util.Locale;

import i5.las2peer.services.ocd.utils.EnumDisplayNames;

/**
 * ClcOutputAdapter registry.
 * Used for factory instantiation, persistence or other context.
 *
 */
public enum ClcOutputFormat implements EnumDisplayNames{
    /**
     * Format corresponding to the XmlClcOutputAdapter.
     */
    XML ("XML", XmlClcOutputAdapter.class, 0),

    /**
     * Format corresponding to the EventListOutputAdapter.
     */
    EVENT_LIST ("Event List", EventListOutputAdapter.class, 1);

    /**
     * The adapter class corresponding to the format.
     */
    private final Class<? extends ClcOutputAdapter> adapterClass;

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
    private ClcOutputFormat(String displayName, Class<? extends ClcOutputAdapter> adapterClass, int id) {
        this.displayName = displayName;
        this.adapterClass = adapterClass;
        this.id = id;
    }

    /**
     * Returns the ClcOutputAdapter subclass corresponding to the type.
     * @return The corresponding class.
     */
    protected final Class<? extends ClcOutputAdapter> getAdapterClass() {
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
    public static ClcOutputFormat lookupFormat(int id) {
        for (ClcOutputFormat format : ClcOutputFormat.values()) {
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