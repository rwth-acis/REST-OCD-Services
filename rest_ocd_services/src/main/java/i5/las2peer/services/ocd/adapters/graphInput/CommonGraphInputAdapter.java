package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.InputAdapter;
import i5.las2peer.services.ocd.graphs.MultiplexGraph;

import java.text.ParseException;
import java.util.Map;


/**
 * The common interface of all graph input adapters.
 * @author Maren
 *
 */
public interface CommonGraphInputAdapter extends InputAdapter {
	public void setParameter(Map<String,String> param) throws IllegalArgumentException, ParseException;

}
