package i5.las2peer.services.ocd.viewer.painters;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * CoverPainter registry.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum CoverPaintingType {

	/*
	 * Each enum constant is instantiated with a corresponding CoverPainter class object and a UNIQUE id.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	/**
	 * Type corresponding to the PredefinedColorsCoverPainter.
	 */
	PREDEFINED_COLORS (PredefinedColorsCoverPainter.class, 0),
	/**
	 * Type corresponding to the RandomColorsCoverPainter.
	 */
	RANDOM_COLORS (RandomColorsCoverPainter.class, 1);
	
	/**
	 * For persistence and other purposes.
	 */
	private final int id;
	
	/**
	 * The class corresponding to the type.
	 */
	private final Class<? extends CoverPainter> painterClass;
	
	/**
	 * Creates a new instance.
	 * @param painterClass Defines the painterClass attribute.
	 * @param id Defines the id attribute.
	 */
	private CoverPaintingType(Class<? extends CoverPainter> painterClass, int id) {
		this.painterClass = painterClass;
		this.id = id;
	}
	
	/**
	 * Returns the CoverPainter subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	protected Class<? extends CoverPainter> getPainterClass() {
		return this.painterClass;
	}
	
	/**
	 * Returns the unique id of the type.
	 * @return The id.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns the type corresponding to an id.
	 * @param id The id.
	 * @return The corresponding type.
	 */
	public static CoverPaintingType lookupType(int id) {
        for (CoverPaintingType type : CoverPaintingType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        throw new InvalidParameterException();
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
