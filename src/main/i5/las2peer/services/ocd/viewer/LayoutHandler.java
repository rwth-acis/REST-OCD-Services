package i5.las2peer.services.ocd.viewer;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.viewer.layouters.GraphLayoutType;
import i5.las2peer.services.ocd.viewer.layouters.GraphLayouter;
import i5.las2peer.services.ocd.viewer.layouters.GraphLayouterFactory;
import i5.las2peer.services.ocd.viewer.painters.CoverPainter;
import i5.las2peer.services.ocd.viewer.painters.CoverPainterFactory;
import i5.las2peer.services.ocd.viewer.painters.CoverPaintingType;

import java.awt.Color;
import java.util.List;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.view.Arrow;
import y.view.DefaultGraph2DRenderer;
import y.view.EdgeLabel;
import y.view.EdgeRealizer;
import y.view.Graph2DView;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.SmartNodeLabelModel;

/**
 * Manages the integration of all layouting phases.
 * @author Sebastian
 *
 */
public class LayoutHandler {

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
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void doLayout(CustomGraph graph, GraphLayoutType layoutType, boolean doLabelNodes, boolean doLabelEdges, 
			double minNodeSize, double maxNodeSize) throws InstantiationException, IllegalAccessException {
		setLayoutDefaults(graph, minNodeSize, maxNodeSize);
		labelGraph(graph, doLabelNodes, doLabelEdges);
		GraphLayouter layouter = graphLayouterFactory.getInstance(layoutType);
		layouter.doLayout(graph);
		setViewDefaults(new Graph2DView(graph));
	}
	
	/**
	 * Sets the default layout attributes for a graph, such as node and edge shapes and node sizes.
	 * @param graph
	 */
	private void setLayoutDefaults(CustomGraph graph, double minNodeSize, double maxNodeSize) {
		NodeCursor nodes = graph.nodes();
		Node node;
		/*
		 * Node size scaling factor
		 */
		double minDegree = graph.getMinWeightedInDegree();
		double maxDegree = graph.getMaxWeightedInDegree();
		double scalingFactor = (maxNodeSize - minNodeSize) / (maxDegree - minDegree);
		while(nodes.ok()) {
			node = nodes.node();
			ShapeNodeRealizer nRealizer = new ShapeNodeRealizer(graph.getRealizer(node));
			graph.setRealizer(node, nRealizer);
			nRealizer.setShapeType(ShapeNodeRealizer.ELLIPSE);
			double curNodeSize = minNodeSize + (graph.getWeightedInDegree(node) - minDegree) * scalingFactor;
			nRealizer.setSize(curNodeSize, curNodeSize);
			nodes.next();
		}
		EdgeCursor edges = graph.edges();
		Edge edge;
		while(edges.ok()) {
			edge = edges.edge();
			EdgeRealizer eRealizer = graph.getRealizer(edge);
			if(graph.isOfType(GraphType.DIRECTED)) {
				eRealizer.setArrow(Arrow.STANDARD);
			}
			edges.next();
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
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void doLayout(Cover cover, GraphLayoutType layoutType, boolean doLabelNodes, boolean doLabelEdges, 
			double minNodeSize, double maxNodeSize, CoverPaintingType paintingType) throws InstantiationException, IllegalAccessException {
		CustomGraph graph = cover.getGraph();
		setLayoutDefaults(graph, minNodeSize, maxNodeSize);
		labelGraph(graph, doLabelNodes, doLabelEdges);
		GraphLayouter layouter = graphLayouterFactory.getInstance(layoutType);
		layouter.doLayout(graph);
		CoverPainter painter = coverPainterFactory.getInstance(paintingType);
		painter.doPaint(cover);
		paintNodes(cover);
		setViewDefaults(new Graph2DView(graph));
	}
	
	/**
	 * Sets the view default attributes, such as the rendering order.
	 * @param view
	 */
	private void setViewDefaults(Graph2DView view) {
		DefaultGraph2DRenderer renderer = new DefaultGraph2DRenderer();
		view.setGraph2DRenderer(renderer);
		renderer.setDrawEdgesFirst(true);
		view.fitContent();
	}
	
	/**
	 * Labels a graph.
	 * @param graph The graph.
	 * @param doLabelNodes Defines whether nodes will receive labels with their names (TRUE) or not (FALSE).
	 * @param doLabelEdges Defines whether edges will receive labels with their weights (TRUE) or not (FALSE).
	 */
	private void labelGraph(CustomGraph graph, boolean doLabelNodes, boolean doLabelEdges) {
		if(doLabelNodes) {
			NodeCursor nodes = graph.nodes();
			while (nodes.ok()) {
				Node node = nodes.node();
				// gets node realizer
				NodeRealizer nRealizer = graph.getRealizer(node);
				// adds name label
				NodeLabel nameLabel = nRealizer.createNodeLabel();
				nameLabel.setText(graph.getNodeName(node));
				SmartNodeLabelModel nameModel = new SmartNodeLabelModel();
				nameLabel.setLabelModel(nameModel, nameModel.createDiscreteModelParameter(SmartNodeLabelModel.POSITION_CENTER));
				nRealizer.addLabel(nameLabel);
				nodes.next();
			}
		}
		if(doLabelEdges) {
			EdgeCursor edges = graph.edges();
			while (edges.ok()) {
				Edge edge = edges.edge();
				// gets edge realizer
				EdgeRealizer eRealizer = graph.getRealizer(edge);
				// adds weight label
				EdgeLabel weightLabel = eRealizer.createEdgeLabel();
				weightLabel.setText(Double.toString(graph.getEdgeWeight(edge)));
				eRealizer.addLabel(weightLabel);
				edges.next();
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
		NodeCursor nodes = graph.nodes();
		float[] curColorCompArray = new float[4];
		float[] colorCompArray;
		Node node;
		while(nodes.ok()) {
			colorCompArray = new float[4];
			node = nodes.node();
			List<Integer> communityIndices = cover.getCommunityIndices(node);
			for(int index : communityIndices) {
				Color comColor = cover.getCommunityColor(index);
				comColor.getRGBComponents(curColorCompArray);
				for(int i=0; i<4; i++) {
					colorCompArray[i] += curColorCompArray[i] * cover.getBelongingFactor(node, index);
				}
			}
			NodeRealizer nRealizer = graph.getRealizer(node);
			nRealizer.setFillColor(new Color(colorCompArray[0], colorCompArray[1], colorCompArray[2], colorCompArray[3]));
			nodes.next();
		}
	}

}
