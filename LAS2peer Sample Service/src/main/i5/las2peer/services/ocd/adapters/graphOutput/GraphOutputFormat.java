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
	GRAPH_ML (GraphMlGraphOutputAdapter.class, 0),
	WEIGHTED_EDGE_LIST (WeightedEdgeListGraphOutputAdapter.class, 1),
	META_XML (MetaXmlGraphOutputAdapter.class, 2);
	
	private final Class<? extends GraphOutputAdapter> adapterClass;
	
	private final int id;
	
	private GraphOutputFormat(Class<? extends GraphOutputAdapter> adapterClass, int id) {
		this.adapterClass = adapterClass;
		this.id = id;
	}
	
	protected Class<? extends GraphOutputAdapter> getAdapterClass() {
		return this.adapterClass;
	}
	
	public int getId() {
		return id;
	}
	
	public static GraphOutputFormat lookupFormat(int id) {
        for (GraphOutputFormat format : GraphOutputFormat.values()) {
            if (id == format.getId()) {
                return format;
            }
        }
        throw new InvalidParameterException();
	}

	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
	
}

