package i5.las2peer.services.ocd.adapters.graphOutput;

import java.security.InvalidParameterException;
import java.util.Locale;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Representation of Graph Output Formats.
 * @author Sebastian
 *
 */
public enum GraphOutputFormat {

	GRAPH_ML (0),
	WEIGHTED_EDGE_LIST (1),
	META_XML (2);
	
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
        throw new InvalidParameterException();
	}
	
	public GraphOutputAdapter getAdapterInstance() {
		switch (this) {
			case GRAPH_ML:
				return new GraphMlGraphOutputAdapter();
			case WEIGHTED_EDGE_LIST:
				return new WeightedEdgeListGraphOutputAdapter();
			case META_XML:
				return new MetaXmlGraphOutputAdapter();
			default:
				throw new NotImplementedException("Graph output adapter not registered.");
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

