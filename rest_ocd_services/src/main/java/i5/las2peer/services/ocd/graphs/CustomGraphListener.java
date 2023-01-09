package i5.las2peer.services.ocd.graphs;

import org.graphstream.graph.implementations.MultiNode;
import org.graphstream.stream.Sink;

/**
 * Listener for Custom Graph Events.
 * Is already integrated into custom graphs and hence should not be readded manually.
 * @author Sebastian
 *
 */
public class CustomGraphListener implements Sink {
	CustomGraph graph;

	public CustomGraphListener(CustomGraph graph) {
		this.graph = graph;
	}


	@Override
	public void edgeAdded(String sourceId, long timeId, String edgeId, String fromNodeId, String toNodeId, boolean directed) {
		//super.edgeAdded(sourceId, timeId, edgeId, fromNodeId, toNodeId, directed);
		graph.addCustomEdge(graph.getEdge(edgeId));
	}

	@Override
	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		//super.edgeRemoved(sourceId, timeId, edgeId);
		graph.removeCustomEdge(graph.getEdge(edgeId));
	}

	@Override
	public void graphCleared(String sourceId, long timeId) {

	}

	@Override
	public void stepBegins(String sourceId, long timeId, double step) {

	}

	@Override
	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		graph.addCustomNode((MultiNode) graph.getNode(nodeId));
	}

	@Override
	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		graph.removeCustomNode((MultiNode) graph.getNode(nodeId));
	}

	@Override
	public void graphAttributeAdded(String sourceId, long timeId, String attribute, Object value) {

	}

	@Override
	public void graphAttributeChanged(String sourceId, long timeId, String attribute, Object oldValue, Object newValue) {

	}

	@Override
	public void graphAttributeRemoved(String sourceId, long timeId, String attribute) {

	}

	@Override
	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId, String attribute, Object value) {

	}

	@Override
	public void nodeAttributeChanged(String sourceId, long timeId, String nodeId, String attribute, Object oldValue, Object newValue) {

	}

	@Override
	public void nodeAttributeRemoved(String sourceId, long timeId, String nodeId, String attribute) {

	}

	@Override
	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId, String attribute, Object value) {

	}

	@Override
	public void edgeAttributeChanged(String sourceId, long timeId, String edgeId, String attribute, Object oldValue, Object newValue) {

	}

	@Override
	public void edgeAttributeRemoved(String sourceId, long timeId, String edgeId, String attribute) {

	}
}


