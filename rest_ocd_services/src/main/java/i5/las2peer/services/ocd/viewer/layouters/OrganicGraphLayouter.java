package i5.las2peer.services.ocd.viewer.layouters;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;


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
		if(graph.isOfType(GraphType.DIRECTED)) {	
//			ParallelEdgeLayouter parallelLayouter = new ParallelEdgeLayouter(layouter);
//			parallelLayouter.setDirectedModeEnabled(true);
//			parallelLayouter.setLineDistance(10);
//			parallelLayouter.setLeadingEdgeAdjustmentEnabled(false);
//			parallelLayouter.doLayout(graph);
		}
		else {
//			OrganicEdgeRouter router = new OrganicEdgeRouter();
//			router.setCoreLayouter(layouter);
//			router.setEdgeNodeOverlapAllowed(false);
//			router.setMinimalDistance(5);
//			router.setRoutingAll(true);
//			router.setUsingBends(false);
//			router.doLayout(graph);
		}
	}

}
