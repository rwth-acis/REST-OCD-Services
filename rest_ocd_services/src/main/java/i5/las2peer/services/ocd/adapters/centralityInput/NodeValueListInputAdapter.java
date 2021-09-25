package i5.las2peer.services.ocd.adapters.centralityInput;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.Adapters;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.base.NodeCursor;

public class NodeValueListInputAdapter extends AbstractCentralityInputAdapter {

	@Override
	public CentralityMap readCentrality(CustomGraph graph) throws AdapterException {
		String nodeName;
		String nodeValue;
		CentralityMap result = new CentralityMap(graph);
		
		// Get all node names from the graph
		List<String> nodeNames = new ArrayList<String>();
		NodeCursor nc = graph.nodes();
		while(nc.ok()) {
			nodeNames.add(graph.getNodeName(nc.node()));
			nc.next();
		}
		
		try {
			List<String> line = Adapters.readLine(reader);
			while (line.size() >= 2) {
				nodeName = line.get(0);
				nodeValue = line.get(1);
				if(nodeNames.contains(nodeName)) {
					nodeNames.remove(nodeName);
					result.getMap().put(nodeName, Double.parseDouble(nodeValue));
				}
				line = Adapters.readLine(reader);
			}
			// Centrality values of all remaining nodes are set to 0
			while(!nodeNames.isEmpty()) {
				nodeName = nodeNames.get(0);
				result.getMap().put(nodeName, 0.0);
				nodeNames.remove(0);
			}
		} catch (Exception e) {
			throw new AdapterException(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
