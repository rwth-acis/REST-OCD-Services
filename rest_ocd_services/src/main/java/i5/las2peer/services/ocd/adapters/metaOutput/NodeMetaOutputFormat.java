package i5.las2peer.services.ocd.adapters.metaOutput;

import i5.las2peer.services.ocd.utils.EnumDisplayNames;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * nodeOutputAdapter registry.
 * Used for factory instantiation, persistence or other context.
 * @author Max Kissgen
 *
 */
public enum NodeMetaOutputFormat implements EnumDisplayNames {

    /*
     * Each enum constant is instantiated with a corresponding nodeOutputAdapter class object and a UNIQUE id.
     * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
     */
    META_JSON ("Meta-JSON", MetaJsonNodeOutputAdapter.class, 0);
    /**
     * The adapter class corresponding to the format.
     */
    private final Class<? extends NodeMetaOutputAdapter> adapterClass;

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
    NodeMetaOutputFormat(String displayName, Class<? extends NodeMetaOutputAdapter> adapterClass, int id) {
        this.displayName = displayName;
        this.adapterClass = adapterClass;
        this.id = id;
    }

    /**
     * Returns the nodeOutputAdapter subclass corresponding to the type.
     * @return The corresponding class.
     */
    protected Class<? extends NodeMetaOutputAdapter> getAdapterClass() {
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
    public static NodeMetaOutputFormat lookupFormat(int id) {
        for (NodeMetaOutputFormat format : NodeMetaOutputFormat.values()) {
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

