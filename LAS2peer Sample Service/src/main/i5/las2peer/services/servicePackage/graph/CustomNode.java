package i5.las2peer.services.servicePackage.graph;

import java.awt.Color;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import y.base.Node;
import y.view.NodeRealizer;

@Entity
public class CustomNode {

	/*
	 * Database column name definitions.
	 */
	protected static final String idColumnName = "INDEX";
	private static final String nameColumnName = "NAME";
	private static final String xColumnName = "X";
	private static final String yColumnName = "Y";
	private static final String widthColumnName = "WIDTH";
	private static final String heightColumnName = "HEIGHT";
	private static final String colorColumnName = "COLOR";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = idColumnName)
	private int id;
	
	@Column(name = nameColumnName)
	private String name;
	
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

	protected void update(CustomGraph graph, Node node) {
		NodeRealizer nRealizer = graph.getRealizer(node);
		this.x = nRealizer.getX();
		this.y = nRealizer.getY();
		this.height = nRealizer.getHeight();
		this.width = nRealizer.getWidth();
		this.color = nRealizer.getFillColor().getRGB();
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
