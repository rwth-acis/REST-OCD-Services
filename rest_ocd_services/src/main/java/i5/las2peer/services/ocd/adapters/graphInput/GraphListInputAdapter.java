package i5.las2peer.services.ocd.adapters.graphInput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.Reader;
import java.text.ParseException;
import java.util.Map;

public class GraphListInputAdapter implements GraphInputAdapter {

    @Override
    public CustomGraph readGraph() throws AdapterException {
        return null;
    }

    @Override
    public void setParameter(Map<String, String> param) throws IllegalArgumentException, ParseException {

    }

    @Override
    public Reader getReader() {
        return null;
    }

    @Override
    public void setReader(Reader reader) {

    }
}
