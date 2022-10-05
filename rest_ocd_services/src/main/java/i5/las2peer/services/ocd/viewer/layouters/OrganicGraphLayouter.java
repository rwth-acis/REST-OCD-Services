package i5.las2peer.services.ocd.viewer.layouters;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import y.layout.ParallelEdgeLayouter;
import y.layout.organic.SmartOrganicLayouter;
import y.layout.router.OrganicEdgeRouter;

import java.util.Iterator;

/**
 * Organic layouter, based on the Organic Layout Style of the yFiles library.
 * @author Sebastian
 *
 */
//TODO: Check how all of the yFiles attributes can even be realized
//TODO: Might have to freeze layout at some point (->when?)
public class OrganicGraphLayouter implements GraphLayouter {

	@Override
	public void doLayout(CustomGraph graph) {
		graph.setAttribute("ui.stylesheet",
				"graph {" +
						"	padding: 2;" +
						"" +
						"}" +
						"node {" +
						"	text-mode: normal;" +
						"}");
		Iterator<Node> nodesIt = graph.nodes().iterator();
		while(nodesIt.hasNext()) {
			Node node = nodesIt.next();
			node.setAttribute("ui.style",node.getAttribute("ui.style") + "stroke-mode: plain;"
					+ "stroke-color: black;"
					+ "stroke-width: 1;");
		}
		if (graph.isOfType(GraphType.DIRECTED)) {
			Iterator<Edge> edgesIt = graph.edges().iterator();
			while(edgesIt.hasNext()) {
				Edge edge = edgesIt.next();
				edge.setAttribute("ui.style", edge.getAttribute("ui.style") + "fill-color: black; shape: line; size: 1.5;");
			}
		} else {
			Iterator<Edge> edgesIt = graph.edges().iterator();
			while(edgesIt.hasNext()) {
				Edge edge = edgesIt.next();
				edge.setAttribute("ui.style", edge.getAttribute("ui.style") + "fill-color: black; shape: line; size: 1.5;");
			}
		}

		graph.layout.shake();
		graph.layout.compute();

		while (graph.layout.getStabilization() < 0.95) {
			graph.layout.compute();
		}
		//For Debugging
		//System.setProperty("org.graphstream.ui", "swing");
		//graph.display();

		//while(true);
		//For Debugging
	}
};
