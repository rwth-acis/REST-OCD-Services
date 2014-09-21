package i5.las2peer.services.ocd.graphs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import y.base.Node;

/**
 * Custom node expansion.
 * Holds node meta information and is used for node persistence.
 * @author Sebastian
 *
 */
@Entity
@IdClass(CustomNodeId.class)
@Table(
		uniqueConstraints=
            @UniqueConstraint(columnNames={CustomNode.idColumnName, CustomNode.graphIdColumnName, CustomNode.nameColumnName})
)
public class CustomNode {

	/*
	 * Database column name definitions.
	 */
	protected static final String idColumnName = "INDEX";
	protected static final String graphIdColumnName = "GRAPH_ID";
	protected static final String graphUserColumnName = "USER_NAME";
	protected static final String nameColumnName = "NAME";
//	private static final String xColumnName = "X";
//	private static final String yColumnName = "Y";
//	private static final String widthColumnName = "WIDTH";
//	private static final String heightColumnName = "HEIGHT";
//	private static final String colorColumnName = "COLOR";
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = idColumnName)
	private int id;

	/**
	 * The graph that the node is part of.
	 */
	@Id
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = graphIdColumnName, referencedColumnName = CustomGraph.idColumnName),
		@JoinColumn(name = graphUserColumnName, referencedColumnName = CustomGraph.userColumnName)
	})
	private CustomGraph graph;
	
	/**
	 * The name of then node.
	 */
	@Column(name = nameColumnName)
	private String name;
	
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////// The following attributes are only of internal use for persistence purposes.
	/////////////////////////////////////////////////////////////////////////////////////////
	
//	/*
//	 * The x coordinate of the visual node representation.
//	 * Only for persistence purposes.
//	 */
//	@Column(name = xColumnName)
//	private double x;
//	
//	/*
//	 * The y coordinate of the visual node representation.
//	 * Only for persistence purposes.
//	 */
//	@Column(name = yColumnName)
//	private double y;
//	
//	/*
//	 * The height of the visual node representation.
//	 * Only for persistence purposes.
//	 */
//	@Column(name = heightColumnName)
//	private double height;
//	
//	/*
//	 * The width of the visual node representation.
//	 * Only for persistence purposes.
//	 */
//	@Column(name = widthColumnName)
//	private double width;
//	
//	/*
//	 * The color of the visual node representation.
//	 * Only for persistence purposes.
//	 */
//	@Column(name = colorColumnName)
//	private int color;  
	
	//////////////////////////////////////////////////////////////////
	//////// Methods
	//////////////////////////////////////////////////////////////////
	
	/**
	 * Creates a new instance.
	 */
	protected CustomNode(){
	}
	
	/**
	 * Copy constructor.
	 * @param customNode The custom node to copy.
	 */
	protected CustomNode(CustomNode customNode) {
		this.name = customNode.name;
	}
	
	/**
	 * Getter for the id.
	 * @return The id.
	 */
	public int getId() {
		return this.id;
	}	
	
	/**
	 * Getter for the node name.
	 * @return The node name.
	 */
	protected String getName() {
		return name;
	}

	/**
	 * Setter for the node name.
	 * @param name The node name.
	 */
	protected void setName(String name) {
		this.name = name;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	/////////// The following attributes are only of internal use for persistence purposes.
	/////////////////////////////////////////////////////////////////////////////////////////
	
//	protected double getX() {
//		return this.x;
//	}
//
//	protected void setX(double x) {
//		this.x = x;
//	}
//
//	protected double getY() {
//		return this.y;
//	}
//
//	protected void setY(double y) {
//		this.y = y;
//	}
//
//	protected double getHeight() {
//		return this.height;
//	}
//
//	protected void setHeight(double height) {
//		this.height = height;
//	}
//
//	protected double getWidth() {
//		return this.width;
//	}
//
//	protected void setWidth(double width) {
//		this.width = width;
//	}
//
//	protected int getColor() {
//		return color;
//	}
//
//	protected void setColor(int color) {
//		this.color = color;
//	}

	/*
	 * Getter for the graph.
	 * Only for persistence purposes.
	 * @return The graph.
	 */
	protected CustomGraph getGraph() {
		return graph;
	}
	
	/*
	 * Updates a custom node before it is being persisted.
	 * Only for persistence purposes.
	 * @param graph The graph that the (custom) node is part of.
	 * @param node The corresponding node.
	 */
	protected void update(CustomGraph graph, Node node) {
//		NodeRealizer nRealizer = graph.getRealizer(node);
//		this.x = nRealizer.getX();
//		this.y = nRealizer.getY();
//		this.height = nRealizer.getHeight();
//		this.width = nRealizer.getWidth();
//		this.color = nRealizer.getFillColor().getRGB();
		this.graph = graph;
	}
	
	/*
	 * Creates a corresponding node after the custom node was loaded from persistence.
	 * Only for persistence purposes.
	 * @param graph The graph that the (custom) node is part of.
	 * @return The created node.
	 */
	protected Node createNode(CustomGraph graph) {
		Node node = graph.createNode();
//		NodeRealizer nRealizer = graph.getRealizer(node);
//		nRealizer.setX(this.x);
//		nRealizer.setY(this.y);
//		nRealizer.setHeight(this.height);
//		nRealizer.setWidth(this.width);
//		nRealizer.setFillColor(new Color(this.color));
		return node;
	}
	
}
