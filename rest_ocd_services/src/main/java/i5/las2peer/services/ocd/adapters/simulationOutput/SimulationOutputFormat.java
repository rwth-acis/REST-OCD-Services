package i5.las2peer.services.ocd.adapters.simulationOutput;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * SimulationOutputAdapter registry.
 * Used for factory instantiation, persistence or other context.
 * Each enum constant is instantiated with a corresponding VisualOutputAdapter class object and a UNIQUE id.
 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
 *
 */
public enum SimulationOutputFormat {

	/**
	 * Format corresponding to the MetaXmlSimulationOutputAdapter
	 */
	META_XML (MetaXmlSimulationOutputAdapter.class, 0);
	
	/**
	 * The adapter class corresponding to the format.
	 */
	private final Class<? extends SimulationOutputAdapter> adapterClass;
	
	/**
	 * Reserved for persistence or other purposes.
	 */
	private final int id;
	
	/**
	 * Creates a new instance.
	 * @param adapterClass Defines the adapterClass attribute.
	 * @param id Defines the id attribute.
	 */
	private SimulationOutputFormat(Class<? extends SimulationOutputAdapter> adapterClass, int id) {
		this.adapterClass = adapterClass;
		this.id = id;
	}
	
	/**
	 * Returns the SimulationOutputAdapter subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	protected Class<? extends SimulationOutputAdapter> getAdapterClass() {
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
	 * Returns the format corresponding to an id.
	 * @param id The id.
	 * @return The corresponding format.
	 */
	public static SimulationOutputFormat lookupFormat(int id) {
        for (SimulationOutputFormat format : SimulationOutputFormat.values()) {
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
