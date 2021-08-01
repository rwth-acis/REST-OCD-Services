package i5.las2peer.services.ocd.viewer.layouters;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * GraphLayouter registry.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum GraphLayoutType {

	/*
	 * Each enum constant is instantiated with a corresponding GraphLayouter class object and a UNIQUE id.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	/**
	 * Type corresponding to the OrganicGraphLayouter.
	 */
	ORGANIC (OrganicGraphLayouter.class, 0);
	
	/**
	 * The class corresponding to the type.
	 */
	private final Class<? extends GraphLayouter> layouterClass;
	
	/**
	 * For persistence and other purposes.
	 */
	private final int id;
	
	/**
	 * Creates a new instance.
	 * @param layouterClass Defines the layouterClass attribute.
	 * @param id Defines the id attribute.
	 */
	private GraphLayoutType(Class<? extends GraphLayouter> layouterClass, int id) {
		this.layouterClass = layouterClass;
		this.id = id;
	}
	
	/**
	 * Returns the GraphLayouter subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	protected Class<? extends GraphLayouter> getLayouterClass() {
		return this.layouterClass;
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
	public static GraphLayoutType lookupType(int id) {
        for (GraphLayoutType type : GraphLayoutType.values()) {
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
