package i5.las2peer.services.ocd.graphs;

import java.awt.Color;

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
import y.view.NodeRealizer;

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
	private static final String xColumnName = "X";
	private static final String yColumnName = "Y";
	private static final String widthColumnName = "WIDTH";
	private static final String heightColumnName = "HEIGHT";
	private static final String colorColumnName = "COLOR";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = idColumnName)
	private int id;

	@Id
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = graphIdColumnName, referencedColumnName = CustomGraph.idColumnName),
		@JoinColumn(name = graphUserColumnName, referencedColumnName = CustomGraph.userColumnName)
	})
	private CustomGraph graph;
	
	@Column(name = nameColumnName)
	private String name;
	
	/*
	 * Attributes from y.base.Node
	 * Only for persistence.
	 */
	@Column(name = xColumnName)
	private double x;
	
	@Column(name = yColumnName)
	private double y;
	
	@Column(name = heightColumnName)
	private double height;
	
	@Column(name = widthColumnName)
	private double width;
	
	@Column(name = colorColumnName)
	private int color;  
	
	protected CustomNode(){
	}
	
	protected CustomNode(CustomNode customNode) {
		this.name = customNode.name;
	}
	
	public int getId() {
		return this.id;
	}	
	
	protected String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected double getX() {
		return this.x;
	}

	protected void setX(double x) {
		this.x = x;
	}

	protected double getY() {
		return this.y;
	}

	protected void setY(double y) {
		this.y = y;
	}

	protected double getHeight() {
		return this.height;
	}

	protected void setHeight(double height) {
		this.height = height;
	}

	protected double getWidth() {
		return this.width;
	}

	protected void setWidth(double width) {
		this.width = width;
	}

	protected int getColor() {
		return color;
	}

	protected void setColor(int color) {
		this.color = color;
	}

	protected CustomGraph getGraph() {
		return graph;
	}
	
	protected void update(CustomGraph graph, Node node) {
		NodeRealizer nRealizer = graph.getRealizer(node);
		this.x = nRealizer.getX();
		this.y = nRealizer.getY();
		this.height = nRealizer.getHeight();
		this.width = nRealizer.getWidth();
		this.color = nRealizer.getFillColor().getRGB();
		this.graph = graph;
	}
	
	protected Node createNode(CustomGraph graph) {
		Node node = graph.createNode();
		NodeRealizer nRealizer = graph.getRealizer(node);
		nRealizer.setX(this.x);
		nRealizer.setY(this.y);
		nRealizer.setHeight(this.height);
		nRealizer.setWidth(this.width);
		nRealizer.setFillColor(new Color(this.color));
		return node;
	}
	
}
