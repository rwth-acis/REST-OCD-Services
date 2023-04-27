package i5.las2peer.services.ocd.adapters.graphInput;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.InputAdapter;
import i5.las2peer.services.ocd.graphs.CustomGraph;


/**
 * The common interface of all graph input adapters.
 * @author Sebastian
 *
 */
public interface GraphInputAdapter extends InputAdapter {
	
	/**
	 * Reads the graph and closes the reader.
	 * Sets a unique name for each node of the graph.
	 * @return The read graph.
	 * @throws AdapterException If the input provided by the reader has invalid format.
	 */
	public CustomGraph readGraph() throws AdapterException;

	/**
	 * From a map of parameter names and their values in String format, set the parameters of the input algorithm
	 * @param param The parameter/value map
	 * @throws IllegalArgumentException if a parameter could not be set
	 * @throws ParseException if a parameter could not be parsed from a value string
	 */
	public void setParameter(Map<String,String> param) throws IllegalArgumentException, ParseException;

	/**
	 * From a map of parameter names and a list of their values in String format, set the parameters of the input algorithm
	 * @param listParam The parameter/list map
	 * @throws IllegalArgumentException if a parameter could not be set
	 * @throws ParseException if a parameter could not be parsed from a value string
	 */
	default void setListParameter(Map<String, List<String>> listParam) throws IllegalArgumentException, ParseException {}
}
