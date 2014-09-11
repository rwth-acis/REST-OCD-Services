package i5.las2peer.services.ocd.adapters.graphInput;

import java.security.InvalidParameterException;
import java.util.Locale;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Representation of Graph Input Formats.
 * @author Sebastian
 *
 */
public enum GraphInputFormat {

	GRAPH_ML (0),
	WEIGHTED_EDGE_LIST (1),
	UNWEIGHTED_EDGE_LIST (2),
	NODE_WEIGHTED_EDGE_LIST (3),
	GML (4);

	
	private final int id;
	
	private GraphInputFormat(int id) {
		this.id = id;
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
	
	public GraphInputAdapter getAdapterInstance() {
		switch (this) {
			case GRAPH_ML:
				return new GraphMlGraphInputAdapter();
			case NODE_WEIGHTED_EDGE_LIST:
				return new NodeWeightedEdgeListGraphInputAdapter();
			case UNWEIGHTED_EDGE_LIST:
				return new UnweightedEdgeListGraphInputAdapter();
			case WEIGHTED_EDGE_LIST:
				return new WeightedEdgeListGraphInputAdapter();
			case GML:
				return new GmlGraphInputAdapter();
			default:
				throw new NotImplementedException("Graph input adapter not registered.");
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
