package i5.las2peer.services.servicePackage.graph;

import y.base.Edge;
import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;

public class CustomGraphListener implements GraphListener {
	
	@Override
	public void onGraphEvent(GraphEvent event) {
		if(event.getGraph() instanceof CustomGraph) {
			CustomGraph graph = (CustomGraph)event.getGraph();
			byte type = event.getType();
			switch(type) {
				case GraphEvent.EDGE_CREATION:
					if(event.getData() instanceof Edge) {
						graph.addCustomEdge((Edge)event.getData());
					}
					break;
				case GraphEvent.PRE_EDGE_REMOVAL:
					if(event.getData() instanceof Edge) {
						graph.removeCustomEdge((Edge)event.getData());
					}
					break;
				case GraphEvent.NODE_CREATION:
					if(event.getData() instanceof Node) {
						graph.addCustomNode((Node)event.getData());
					}
					break;
				case GraphEvent.PRE_NODE_REMOVAL:
					if(event.getData() instanceof Node) {
						graph.removeCustomNode((Node)event.getData());
					}
					break;
				default:
			}
		}
	}

}
