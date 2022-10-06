package i5.las2peer.services.ocd.viewer;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.viewer.layouters.GraphLayouter;
import i5.las2peer.services.ocd.viewer.layouters.GraphLayouterFactory;
import i5.las2peer.services.ocd.viewer.layouters.GraphLayoutType;
import i5.las2peer.services.ocd.viewer.painters.CoverPainter;
import i5.las2peer.services.ocd.viewer.painters.CoverPainterFactory;
import i5.las2peer.services.ocd.viewer.painters.CoverPaintingType;
import i5.las2peer.services.ocd.viewer.utils.CentralityVisualizationType;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;

/**
 * Manages the integration of all layouting phases.
 * @author Sebastian
 *
 */
//TODO: Currently we need to work solely with the ui.style attribute of elements since the SVG visualization of graphstream refuses to look at other attributes, we should change to use single attributes when it it is possible
public class LayoutHandler {
	private static final Color CENTRALITY_COLOR = Color.BLUE;
	private static final Color CENTRALITY_GRADIENT_MIN = Color.GREEN;
	private static final Color CENTRALITY_GRADIENT_MAX = Color.RED;
	private static final double MIN_NODE_SIZE = 20;
	private static final double MAX_NODE_SIZE = 50;

	/**
	 * The factory used to create cover painters.
	 */
	private CoverPainterFactory coverPainterFactory = new CoverPainterFactory();
	
	/**
	 * The factory used to create graph layouters.
	 */
	private GraphLayouterFactory graphLayouterFactory = new GraphLayouterFactory();
	
	/**
	 * Applies a layout a graph.
	 * @param graph The graph.
	 * @param layoutType The layout type defining which graph layouter to use.
	 * @param doLabelNodes Defines whether nodes will receive labels with their names (TRUE) or not (FALSE).
	 * @param doLabelEdges Defines whether edges will receive labels with their weights (TRUE) or not (FALSE).
	 * @param minNodeSize Defines the minimum size of a node. Must be greater than 0.
	 * @param maxNodeSize Defines the maximum size of a node. Must be at least as high as the defined minimum size.
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	public void doLayout(CustomGraph graph, GraphLayoutType layoutType, boolean doLabelNodes, boolean doLabelEdges, 
			double minNodeSize, double maxNodeSize) throws InstantiationException, IllegalAccessException, InterruptedException {
		setLayoutDefaults(graph, minNodeSize, maxNodeSize);
		labelGraph(graph, doLabelNodes, doLabelEdges);
		GraphLayouter layouter = graphLayouterFactory.getInstance(layoutType);
		layouter.doLayout(graph);
		setViewDefaults(graph);
	}
	
	/**
	 * Sets the default layout attributes for a graph, such as node and edge shapes and node sizes.
	 * @param graph the graph
	 */
	private void setLayoutDefaults(CustomGraph graph, double minNodeSize, double maxNodeSize) throws InterruptedException {
		Iterator<Node> nodesIt = graph.iterator();
		Node node;
		/*
		 * Node size scaling factor
		 */
		double minDegree = graph.getMinWeightedInDegree();
		double maxDegree = graph.getMaxWeightedInDegree();
		double degreeDifference = (maxDegree == minDegree) ? 1.0 : (maxDegree - minDegree);
		double scalingFactor = (maxNodeSize - minNodeSize) / degreeDifference;
		while(nodesIt.hasNext()) {
			node = nodesIt.next();

			double curNodeSize = minNodeSize + (graph.getWeightedInDegree(node) - minDegree) * scalingFactor;
			//"Declare" nodestyles
			node.setAttribute("ui.style", "fill-color: rgba(" + 200 + "," + 200 + "," + 240 + "," + 255 + ");"
					+ "shape: circle; size: "+ curNodeSize +";");
		}

		String arrowShape = "none";
		if(graph.isOfType(GraphType.DIRECTED)) {
			arrowShape = "arrow";
		}
		Iterator<Edge> edgesIt = graph.edges().iterator();

		while(edgesIt.hasNext()) {
			Edge edge = edgesIt.next();
			edge.setAttribute("ui.style", "arrow-shape: "+ arrowShape + ";"); //TODO: Doesnt currently have any effect since we have to use graphstream directed type edges and SVG does not style them based on the style attribute
		}
	}
	
	/**
	 * Applies a layout the graph of a cover.
	 * @param cover The cover.
	 * @param layoutType The layout type defining which graph layouter to use.
	 * @param doLabelNodes Defines whether nodes will receive labels with their names (TRUE) or not (FALSE).
	 * @param doLabelEdges Defines whether edges will receive labels with their weights (TRUE) or not (FALSE).
	 * @param minNodeSize Defines the minimum size of a node. Must be greater than 0.
	 * @param maxNodeSize Defines the maximum size of a node. Must be at least as high as the defined minimum size.
	 * @param paintingType The painting type defining which cover painter to use.
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
	 * @throws InterruptedException If the executing thread was interrupted.
	 */
	public void doLayout(Cover cover, GraphLayoutType layoutType, boolean doLabelNodes, boolean doLabelEdges, 
			double minNodeSize, double maxNodeSize, CoverPaintingType paintingType) throws InstantiationException, IllegalAccessException, InterruptedException {
		CustomGraph graph = cover.getGraph();
		setCoverLayoutDefaults(graph, minNodeSize, maxNodeSize);
		labelGraph(graph, doLabelNodes, doLabelEdges);
		GraphLayouter layouter = graphLayouterFactory.getInstance(layoutType);
		layouter.doLayout(graph);
		CoverPainter painter = coverPainterFactory.getInstance(paintingType);
		painter.doPaint(cover);
		paintNodes(cover);
		setViewDefaults(graph);
	}

	/**
	 * Sets the default layout attributes for a graph visualizing a cover, such as node and edge shapes and node sizes.
	 * @param graph the graph
	 */
	private void setCoverLayoutDefaults(CustomGraph graph, double minNodeSize, double maxNodeSize) throws InterruptedException {
		Iterator<Node> nodesIt = graph.iterator();
		Node node;
		/*
		 * Node size scaling factor
		 */
		double minDegree = graph.getMinWeightedInDegree();
		double maxDegree = graph.getMaxWeightedInDegree();
		double degreeDifference = (maxDegree == minDegree) ? 1.0 : (maxDegree - minDegree);
		double scalingFactor = (maxNodeSize - minNodeSize) / degreeDifference;
		while(nodesIt.hasNext()) {
			node = nodesIt.next();
			double curNodeSize = minNodeSize + (graph.getWeightedInDegree(node) - minDegree) * scalingFactor;
			//"Declare" nodestyles
			node.setAttribute("ui.style", "shape: circle; size: "+ curNodeSize +";");
			//TODO: Ideally we shouldn't need a separate ui.size attribute and should be able to use the size from ui.style
			node.setAttribute("ui.size", curNodeSize); // set directly accessible attribute for node size
		}

		String arrowShape = "none";
		if(graph.isOfType(GraphType.DIRECTED)) {
			arrowShape = "arrow";
		}
		Iterator<Edge> edgesIt = graph.edges().iterator();

		while(edgesIt.hasNext()) {
			Edge edge = edgesIt.next();
			edge.setAttribute("ui.style", "arrow-shape: "+ arrowShape + ";");
		}
	}
	
	/**
	 * Applies a layout the graph of a CentralityMap.
	 * @param map The CentralityMap.
	 * @param layoutType The layout type defining which graph layouter to use.
	 * @param doLabelNodes Defines whether nodes will receive labels with their names (TRUE) or not (FALSE).
	 * @param doLabelEdges Defines whether edges will receive labels with their weights (TRUE) or not (FALSE).
	 * @param centralityVisualizationType The type of visualization to represent the centrality values.
	 * @throws InstantiationException if instantiation failed
	 * @throws IllegalAccessException if an illegal access occurred on the instance
	 */
	public void doLayout(CentralityMap map, GraphLayoutType layoutType, boolean doLabelNodes, boolean doLabelEdges, 
			CentralityVisualizationType centralityVisualizationType) throws InstantiationException, IllegalAccessException {
		CustomGraph graph = map.getGraph();
		setCentralityLayoutDefaults(graph);
		labelGraph(graph, doLabelNodes, doLabelEdges);
		GraphLayouter layouter = graphLayouterFactory.getInstance(layoutType);
		layouter.doLayout(graph);
		switch(centralityVisualizationType) {
		case COLOR_SATURATION:
			paintNodesWithSingleColor(map);
			break;
		case HEAT_MAP:
			paintNodesWithColorGradient(map);
			break;
		case NODE_SIZE:
			setProportionalNodeSizes(map);
			break;
		}
		setViewDefaults(graph);
	}

	/**
	 * Sets the layout attributes of a graph for visualizing a CentralityMap.
	 * @param graph The graph of the CentralityMap that is visualized
	 */
	private void setCentralityLayoutDefaults(CustomGraph graph) {
		Iterator<Node> nodesIt = graph.iterator();
		while(nodesIt.hasNext()) {
			Node node = nodesIt.next();
			node.setAttribute("ui.shape", "circle");
		}

		String arrowShape = "none";
		if(graph.isOfType(GraphType.DIRECTED)) {
			arrowShape = "arrow";
		}
		Iterator<Edge> edgesIt = graph.edges().iterator();
		while(edgesIt.hasNext()) {
			Edge edge = edgesIt.next();
			edge.setAttribute("ui.style", "arrow-shape: "+ arrowShape + ";");
		}
	}
	
	/**
	 * Sets the view default attributes, such as the rendering order.
	 * @param graph the graph view
	 */
	private void setViewDefaults(CustomGraph graph) {
		graph.setAttribute("ui.stylesheet",
				"node {" +
				"	z-index: 2;" +
				"}" +
				"edge {" +
				"	z-index: 1;" +
				"}");

	}
	
	/**
	 * Labels a graph.
	 * @param graph The graph.
	 * @param doLabelNodes Defines whether nodes will receive labels with their names (TRUE) or not (FALSE).
	 * @param doLabelEdges Defines whether edges will receive labels with their weights (TRUE) or not (FALSE).
	 */
	private void labelGraph(CustomGraph graph, boolean doLabelNodes, boolean doLabelEdges) {
		if(doLabelNodes) {
			Iterator<Node> nodes = graph.iterator();
			while (nodes.hasNext()) {
				Node node = nodes.next();
				// adds name label
				node.setAttribute("label", graph.getNodeName(node)); //For SVG Viz label needs to not have "ui." in front, for graphstream desktop UIs it does
				node.setAttribute("ui.style",node.getAttribute("ui.style") + "text-alignment: center;"
						+ "text-size: 12;"
						+ "text-style: bold;"
						+ "text-font: Arial;");
			}
		}

		Iterator<Edge> edgesIt = graph.edges().iterator();
		while (edgesIt.hasNext()) {
			Edge edge = edgesIt.next();
			// adds weight label

			if(doLabelEdges) {
				edge.setAttribute("label", graph.getEdgeWeight(edge)); //For SVG Viz label needs to not have "ui." in front, for graphstream desktop UIs it does
				edge.setAttribute("ui.style",edge.getAttribute("ui.style") + "text-alignment: along;"
						+ "text-size: 12;"
						+ "text-style: bold;"
						+ "text-font: Arial;");
			}
		}
	}
	
	/**
	 * Applies the community colors of a cover to the nodes of the corresponding graph.
	 * The color of an overlapping node is obtained by a weighted mix
	 * of the corresponding community colors in accordance with the node's
	 * membership degrees / belonging factors.
	 * @param cover The cover.
	 */
	private void paintNodes(Cover cover) {
		CustomGraph graph = cover.getGraph();
		Iterator<Node> nodesIt = graph.iterator();
		float[] curColorCompArray = new float[4];
		float[] colorCompArray;
		Node node;
		while(nodesIt.hasNext()) {
			colorCompArray = new float[4];
			node = nodesIt.next();
			List<Integer> communityIndices = cover.getCommunityIndices(node);
			for(int index : communityIndices) {
				Color comColor = cover.getCommunityColor(index);
				comColor.getRGBComponents(curColorCompArray);
				for(int i=0; i<4; i++) {
					colorCompArray[i] += curColorCompArray[i] * cover.getBelongingFactor(node, index);
				}
			}
			//TODO: Make nicer so that java color is not needed
			Color color = new Color(colorCompArray[0], colorCompArray[1], colorCompArray[2], colorCompArray[3]);
			node.setAttribute("ui.style",node.getAttribute("ui.style") + "fill-color: rgba(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha() + ");");
			//TODO: Ideally we shouldn't need a separate ui.fill-color attribute and should be able to use the value from ui.style
			node.setAttribute("ui.fill-color", new float[]{color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()/255}); // set directly accessible color attribute for node
		}
	}
	
	/**
	 * Paint the nodes using a single color, setting the saturation proportionally to the centrality value of the node.
	 * @param map The CentralityMap that is visualized.
	 * @author Tobias
	 */
	private void paintNodesWithSingleColor(CentralityMap map) {
		int r = CENTRALITY_COLOR.getRed();
		int g = CENTRALITY_COLOR.getGreen();
		int b = CENTRALITY_COLOR.getBlue();
		float[] hsbValues = Color.RGBtoHSB(r, g, b, null);	
		double min = map.getMinValue();
		double max = map.getMaxValue();
		CustomGraph graph = map.getGraph();
		Iterator<Node> nodesIt = graph.iterator();
		while(nodesIt.hasNext()) {
			Node node = nodesIt.next();
			float nodeSaturation = (float) ((map.getNodeValue(node) - min) / (max - min));
			Color color = Color.getHSBColor(hsbValues[0], nodeSaturation, hsbValues[2]); // Use HSB for saturation here
			node.setAttribute("ui.style",node.getAttribute("ui.style") + "fill-color: rgba(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha() + ");");
		}
	}
	
	/**
	 * Paint the nodes using a color gradient where green represents a low centrality value and red a high centrality value.
	 * @param map The CentralityMap that is visualized.
	 * @author Tobias
	 */
	private void paintNodesWithColorGradient(CentralityMap map) {
		int rMin = CENTRALITY_GRADIENT_MIN.getRed();
		int gMin = CENTRALITY_GRADIENT_MIN.getGreen();
		int bMin = CENTRALITY_GRADIENT_MIN.getBlue();
		float[] hsbValuesMin = Color.RGBtoHSB(rMin, gMin, bMin, null);
		int rMax = CENTRALITY_GRADIENT_MAX.getRed();
		int gMax = CENTRALITY_GRADIENT_MAX.getGreen();
		int bMax = CENTRALITY_GRADIENT_MAX.getBlue();
		float[] hsbValuesMax = Color.RGBtoHSB(rMax, gMax, bMax, null);
		
		double min = map.getMinValue();
		double max = map.getMaxValue();
		CustomGraph graph = map.getGraph();
		Iterator<Node> nodesIt = graph.iterator();
		while(nodesIt.hasNext()) {
			Node node = nodesIt.next();
			double centralityValue = map.getNodeValue(node);
			float hue = (float) (hsbValuesMin[0] + (hsbValuesMax[0] - hsbValuesMin[0]) * (centralityValue - min) / (max - min));
			Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
			node.setAttribute("ui.style",node.getAttribute("ui.style") + "fill-color: rgba(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha() + ");");
		}
	}
	
	/**
	 * Set the size of each node proportionally to its centrality value.
	 * @param map The CentralityMap that is visualized.
	 * @author Tobias
	 */
	private void setProportionalNodeSizes(CentralityMap map) {
		double min = map.getMinValue();
		double max = map.getMaxValue();
		CustomGraph graph = map.getGraph();
		Iterator<Node> nodesIt = graph.iterator();
		while(nodesIt.hasNext()) {
			Node node = nodesIt.next();
			double centralityValue = map.getNodeValue(node);
			double nodeSize = MIN_NODE_SIZE + (MAX_NODE_SIZE - MIN_NODE_SIZE) * (centralityValue - min) / (max - min);
			node.setAttribute("ui.style",node.getAttribute("ui.style") + "size:" + nodeSize +";");
		}
	}

}
