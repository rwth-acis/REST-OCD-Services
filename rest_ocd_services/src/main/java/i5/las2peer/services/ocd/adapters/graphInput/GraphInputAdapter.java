package i5.las2peer.services.ocd.adapters.graphInput;

import java.text.ParseException;
import java.util.Map;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.InputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.apache.poi.ss.formula.functions.T;


/**
 * The common interface of all customgraph input adapters.
 * @author Sebastian
 *
 */
public interface GraphInputAdapter extends CommonGraphInputAdapter<CustomGraph> {
	
	/**
	 * Reads the graph and closes the reader.
	 * Sets a unique name for each node of the graph.
	 * @return The read graph.
	 * @throws AdapterException If the input provided by the reader has invalid format.
	 */
	public CustomGraph readGraph() throws AdapterException;
}
