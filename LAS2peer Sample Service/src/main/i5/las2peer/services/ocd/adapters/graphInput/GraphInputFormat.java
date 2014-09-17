package i5.las2peer.services.ocd.adapters.graphInput;

import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * GraphInputAdapter registry.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum GraphInputFormat {

	/*
	 * Each enum constant is instantiated with a corresponding GraphInputAdapter class object and a UNIQUE id.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	GRAPH_ML (GraphMlGraphInputAdapter.class, 0),
	WEIGHTED_EDGE_LIST (WeightedEdgeListGraphInputAdapter.class, 1),
	UNWEIGHTED_EDGE_LIST (UnweightedEdgeListGraphInputAdapter.class, 2),
	NODE_WEIGHTED_EDGE_LIST (NodeWeightedEdgeListGraphInputAdapter.class, 3),
	GML (GmlGraphInputAdapter.class, 4);

	private final Class<? extends GraphInputAdapter> adapterClass;
	
	private final int id;
	
	private GraphInputFormat(Class<? extends GraphInputAdapter> adapterClass, int id) {
		this.id = id;
		this.adapterClass = adapterClass;
	}
	
	protected Class<? extends GraphInputAdapter> getAdapterClass() {
		return this.adapterClass;
	}
	
	public int getId() {
		return id;
	}
	
	public static GraphInputFormat lookupFormat(int id) {
        for (GraphInputFormat format : GraphInputFormat.values()) {
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
