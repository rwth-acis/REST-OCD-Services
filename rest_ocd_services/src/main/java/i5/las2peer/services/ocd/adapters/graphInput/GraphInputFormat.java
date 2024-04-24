package i5.las2peer.services.ocd.adapters.graphInput;

import java.security.InvalidParameterException;
import java.util.Locale;

import i5.las2peer.services.ocd.utils.EnumDisplayNames;

/**
 * GraphInputAdapter registry.
 * Used for factory instantiation, persistence or other context.
 * @author Sebastian
 *
 */
public enum GraphInputFormat implements EnumDisplayNames {

	/*
	 * Each enum constant is instantiated with a corresponding GraphInputAdapter class object and a UNIQUE id.
	 * Once the framework is in use ids must not be changed to avoid corrupting the persisted data.
	 */
	/**
	 * Format corresponding to the GraphMlGraphInputAdapter.
	 */
	GRAPH_ML ("GraphML", GraphMlGraphInputAdapter.class, 0),
	/**
	 * Format corresponding to the WeightedEdgeListGraphInputAdapter.
	 */
	WEIGHTED_EDGE_LIST ("Weighted Edge List", WeightedEdgeListGraphInputAdapter.class, 1),
	/**
	 * Format corresponding to the UnweightedEdgeListGraphInputAdapter.
	 */
	UNWEIGHTED_EDGE_LIST ("Unweighted Edge List", UnweightedEdgeListGraphInputAdapter.class, 2),
	/**
	 * Format corresponding to the NodeWeightedEdgeListGraphInputAdapter.
	 */
	NODE_WEIGHTED_EDGE_LIST ("Node Weighted Edge List", NodeWeightedEdgeListGraphInputAdapter.class, 3),
	/**
	 * Format corresponding to the GmlGraphInputAdapter.
	 */
	GML ("GML", GmlGraphInputAdapter.class, 4),
	/**
	 * Format corresponding to the NodeContentListGraphInputAdapter.
	 */
	NODE_CONTENT_EDGE_LIST ("Node Content Edge List", NodeContentEdgeListGraphInputAdapter.class,5),	
	/**
	 * Format corresponding to the XMLGraphInputAdapter.
	 */
	XML ("XML", XMLGraphInputAdapter.class, 6),
	/**
	 * Format corresponding to the XGMMLGraphInputAdapter.
	 */
	XGMML ("XGMML", XGMMLGraphInputAdapter.class, 7),
	/**
	 * Format corresponding to the AdjacencyMatrixGraphInputAdapter.
	 */
	ADJACENCY_MATRIX ("Adjacency Matrix", AdjacencyMatrixGraphInputAdapter.class, 8),
	/**
	 * Format corresponding to the LmsTripleStoreGraphInputAdapter.
	 */
	LMS_TRIPLESTORE ("Fetched from LMS Triplestore", LmsTripleStoreGraphInputAdapter.class, 9),
	/**
	 * Format corresponding to the TimestampedEdgeListInputAdapter.
	 */
	TIMESTAMPED_EDGE_LIST("Timestamped Edge List", TimestampedEdgeListInputAdapter.class, 10);
	/**
	 * The adapter class corresponding to the format.
	 */
	private final Class<? extends GraphInputAdapter> adapterClass;
	
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
	private GraphInputFormat(String displayName, Class<? extends GraphInputAdapter> adapterClass, int id) {
		this.displayName = displayName;
		this.id = id;
		this.adapterClass = adapterClass;
	}
	
	/**
	 * Returns the GraphInputAdapter subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	protected Class<? extends GraphInputAdapter> getAdapterClass() {
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
	public static GraphInputFormat lookupFormat(int id) {
        for (GraphInputFormat format : GraphInputFormat.values()) {
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
