package i5.las2peer.services.ocd.utils;

import java.util.Locale;

import i5.las2peer.services.ocd.utils.EnumDisplayNames;

/**
 * Used to indicate the type of event.
 */
public enum CommunityEventType implements EnumDisplayNames {
    //event types, can be expanded
    GROWTH("Growth", 0),

    BIRTH("Birth", 1),

    CONTRACTION("Contraction", 2),

    DEATH("Death", 3),

    FUSION("Fusion", 4),

    SPLIT("Split", 5);


    /**
     * For persistence and other purposes.
     */
    private int id;

    /**
     * A display name for web frontends and more
     */
    private final String displayName;

    /**
     * Creates a new instance.
     * @param id Defines the id attribute.
     */
    private CommunityEventType(String displayName, int id) {
        this.displayName = displayName;
        this.id = id;
    }

    /**
     * Getter for the id.
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
     * Returns the type corresponding to an id.
     * @param id The id.
     * @return The type.
     */
    public static CommunityEventType lookupType(int id) {
        for (CommunityEventType type : CommunityEventType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        return null;
    }

    /**
     * Returns the name of the type written in lower case letters and with any underscores replaced by space characters.
     */
    @Override
    public String toString() {
        String name = name();
        name = name.replace('_', ' ');
        name = name.toLowerCase(Locale.ROOT);
        return name;
    }
}
