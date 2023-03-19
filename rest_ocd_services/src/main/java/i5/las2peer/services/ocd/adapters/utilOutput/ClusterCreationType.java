package i5.las2peer.services.ocd.adapters.utilOutput;

import i5.las2peer.services.ocd.adapters.visualOutput.JsonVisualOutputAdapter;
import i5.las2peer.services.ocd.adapters.visualOutput.SvgVisualOutputAdapter;
import i5.las2peer.services.ocd.adapters.visualOutput.VisualOutputAdapter;
import i5.las2peer.services.ocd.adapters.visualOutput.VisualOutputFormat;
import i5.las2peer.services.ocd.utils.EnumDisplayNames;

import java.security.InvalidParameterException;
import java.util.Locale;

import java.security.InvalidParameterException;
import java.util.Locale;

import i5.las2peer.services.ocd.utils.EnumDisplayNames;

/**
 * ClusterOutputAdapter registry.
 * Used for factory instantiation, persistence or other context.
 *
 * @author Max
 */
public enum ClusterOutputFormat implements EnumDisplayNames {
    /*
     * Each enum constant is instantiated with a corresponding VisualOutputAdapter class object and a UNIQUE id.
     * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
     */
    /**
     * Format corresponding to the SvgVisualOutputAdapter.
     */
    BY_ATTRIBUTE("By Attribute", AttributeClusterOutputAdapter.class, 0);


    /**
     * The adapter class corresponding to the format.
     */
    private final Class<? extends ClusterOutputAdapter> adapterClass;

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
     *
     * @param adapterClass Defines the adapterClass attribute.
     * @param id           Defines the id attribute.
     */
    private ClusterOutputFormat(String displayName, Class<? extends ClusterOutputAdapter> adapterClass, int id) {
        this.displayName = displayName;
        this.adapterClass = adapterClass;
        this.id = id;
    }

    /**
     * Returns the VisualOutputAdapter subclass corresponding to the type.
     *
     * @return The corresponding class.
     */
    protected Class<? extends ClusterOutputAdapter> getAdapterClass() {
        return this.adapterClass;
    }

    /**
     * Returns the unique id of the format.
     *
     * @return The id.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the display name of the type.
     *
     * @return The name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the format corresponding to an id.
     *
     * @param id The id.
     * @return The corresponding format.
     */
    public static i5.las2peer.services.ocd.adapters.utilOutput.ClusterOutputFormat lookupFormat(int id) {
        for (i5.las2peer.services.ocd.adapters.utilOutput.ClusterOutputFormat format : i5.las2peer.services.ocd.adapters.utilOutput.ClusterOutputFormat.values()) {
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
