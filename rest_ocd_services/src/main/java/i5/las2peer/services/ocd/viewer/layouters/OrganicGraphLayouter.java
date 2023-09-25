package i5.las2peer.services.ocd.viewer.layouters;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.utils.Pair;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Organic layouter, based on the Organic Layout Style of the yFiles library.
 * 
 * @author Sebastian
 *
 */
// TODO: Check how all of the yFiles attributes can even be realized
// TODO: Might have to freeze layout at some point (->when?)
public class OrganicGraphLayouter implements GraphLayouter {

	@Override
	public void doLayout(CustomGraph graph) {
		int numberOfLayers = graph.getNumberOfLayers();
		int biggestEdgeSize = Integer.min(2, numberOfLayers);
		graph.setAttribute("ui.stylesheet",
				"graph {" +
						"	padding: 2;" +
						"" +
						"}" +
						"node {" +
						"	text-mode: normal;" +
						"}");
		Iterator<Node> nodesIt = graph.nodes().iterator();
		while (nodesIt.hasNext()) {
			Node node = nodesIt.next();
			node.setAttribute("ui.style", node.getAttribute("ui.style") + "stroke-mode: plain;"
					+ "stroke-color: black;"
					+ "stroke-width: 1;");
		}
		if (graph.isOfType(GraphType.DIRECTED)) {
			Iterator<Edge> edgesIt = graph.edges().iterator();
			while (edgesIt.hasNext()) {
				Edge edge = edgesIt.next();
				edge.setAttribute("ui.style",
						edge.getAttribute("ui.style") + "fill-color: black; shape: line; size: 1.5;");
			}
		} else {
			Iterator<Edge> edgesIt = graph.edges().iterator();
			if (graph.getTypes().contains(GraphType.MULTIPLE_EDGES)) {
				Iterator<Node> nodesIt1 = graph.nodes().iterator();
				List<Node> nodes2 = new ArrayList<>(graph.nodes().toList());
				while (nodesIt1.hasNext()) {
					MultiNode node1 = (MultiNode) nodesIt1.next();
					for (Node node2 : new ArrayList<>(nodes2)) {
						if (node1.getEdgeSetBetween(node2).size() > 0) {

							Edge edge = node1.getEdgeToward(node2);
							int edgeSize = biggestEdgeSize
									* (node1.getEdgeSetBetween(node2).size() / numberOfLayers);
							edge.setAttribute("ui.style",
									edge.getAttribute("ui.style") + "fill-color: black; shape: line; size: " + edgeSize
											+ ";");
							Edge reverseEdge = node2.getEdgeToward((Node) node1);
							reverseEdge.setAttribute("ui.style",
									reverseEdge.getAttribute("ui.style") + "fill-color: black; shape: line; size: " + edgeSize
											+ ";");
						}
					}
					nodes2.remove((Node) node1);
				}

			} else {
				while (edgesIt.hasNext()) {
					Edge edge = edgesIt.next();
					edge.setAttribute("ui.style",
							edge.getAttribute("ui.style") + "fill-color: black; shape: line; size: 1.5;");
				}
			}
		}

		graph.layout.shake();
		graph.layout.compute();

		while (graph.layout.getStabilization() < 0.95) {
			graph.layout.compute();
		}
		// For Debugging
		// System.setProperty("org.graphstream.ui", "swing");
		// graph.display();

		// while(true);
		// For Debugging
	}

}
