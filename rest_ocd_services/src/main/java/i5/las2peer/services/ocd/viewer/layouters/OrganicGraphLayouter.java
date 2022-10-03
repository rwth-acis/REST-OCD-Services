package i5.las2peer.services.ocd.viewer.layouters;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

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
//		SmartOrganicLayouter layouter = new SmartOrganicLayouter();
//		layouter.setMinimalNodeDistance(45);
//		layouter.setConsiderNodeLabelsEnabled(true);
//		layouter.setNodeOverlapsAllowed(false);
//		layouter.setCompactness(0.2);
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
//			ParallelEdgeLayouter parallelLayouter = new ParallelEdgeLayouter(layouter);
//			parallelLayouter.setDirectedModeEnabled(true);
//			parallelLayouter.setLineDistance(10);
//			parallelLayouter.setLeadingEdgeAdjustmentEnabled(false);
//			parallelLayouter.doLayout(graph);
		} else {
			Iterator<Edge> edgesIt = graph.edges().iterator();
			while(edgesIt.hasNext()) {
				Edge edge = edgesIt.next();
				edge.setAttribute("ui.style", edge.getAttribute("ui.style") + "fill-color: black; shape: line; size: 1.5;");
			}
//			OrganicEdgeRouter router = new OrganicEdgeRouter();
//			router.setCoreLayouter(layouter);
//			router.setEdgeNodeOverlapAllowed(false);
//			router.setMinimalDistance(5);
//			router.setRoutingAll(true);
//			router.setUsingBends(false);
//			router.doLayout(graph);
		}

		//graph.layout.addAttributeSink(graph);
		graph.layout.shake();
		graph.layout.compute();

		while (graph.layout.getStabilization() < 1.0) {
			graph.layout.compute();
		}
		//TODO:REMOVE
		//System.setProperty("org.graphstream.ui", "swing");
		//graph.display();

		//while(true);
		//TODO:REMOVE
	}

}
