package i5.las2peer.services.ocd.adapters.graphOutput;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * GraphOutputAdapter registry.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum GraphOutputFormat {

	/*
	 * Each enum constant is instantiated with a corresponding GraphOutputAdapter class object and a UNIQUE id.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	/**
	 * Format corresponding to the GraphMlGraphOutputAdapter.
	 */
	GRAPH_ML (GraphMlGraphOutputAdapter.class, 0),
	/**
	 * Format corresponding to the WeightedEdgeListGraphOutputAdapter.
	 */
	WEIGHTED_EDGE_LIST (WeightedEdgeListGraphOutputAdapter.class, 1),
	/**
	 * Format corresponding to the MetaXmlGraphOutputAdapter.
	 */
	META_XML (MetaXmlGraphOutputAdapter.class, 2);
	
	/**
	 * The adapter class corresponding to the format.
	 */
	private final Class<? extends GraphOutputAdapter> adapterClass;
	
	/**
	 * Reserved for persistence or other purposes.
	 */
	private final int id;
	
	/**
	 * Creates a new instance.
	 * @param adapterClass Defines the adapterClass attribute.
	 * @param id Defines the id attribute.
	 */
	private GraphOutputFormat(Class<? extends GraphOutputAdapter> adapterClass, int id) {
		this.adapterClass = adapterClass;
		this.id = id;
	}
	
	/**
	 * Returns the GraphOutputAdapter subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	protected Class<? extends GraphOutputAdapter> getAdapterClass() {
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
	public static GraphOutputFormat lookupFormat(int id) {
        for (GraphOutputFormat format : GraphOutputFormat.values()) {
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

