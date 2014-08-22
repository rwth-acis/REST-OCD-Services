package i5.las2peer.services.servicePackage.adapters.graphOutput;

import java.util.Locale;

/**
 * Representation of Graph Output Formats.
 * @author Sebastian
 *
 */
public enum GraphOutputFormat {

	GRAPH_ML (0),
	WEIGHTED_EDGE_LIST (1);
	
	private final int id;
	
	private GraphOutputFormat(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public static GraphOutputFormat lookupType(int id) {
        for (GraphOutputFormat format : GraphOutputFormat.values()) {
            if (id == format.getId()) {
                return format;
            }
        }
        return null;
	}
	
	public GraphOutputAdapter getAdapterInstance() {
		switch (this) {
			case GRAPH_ML:
				return new GraphMlGraphOutputAdapter();
			case WEIGHTED_EDGE_LIST:
				return new WeightedEdgeListGraphOutputAdapter();
			default:
				return null;
		}
	}

	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
	
}

