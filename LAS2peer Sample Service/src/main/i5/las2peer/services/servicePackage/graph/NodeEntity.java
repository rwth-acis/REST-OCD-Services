package i5.las2peer.services.servicePackage.graph;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import y.base.Node;
import y.view.NodeRealizer;

@Embeddable
public class NodeEntity {

	@Column
	private int index;
	
	@Column
	private double x;
	
	@Column
	private double y;
	
	@Column
	private double height;
	
	@Column
	private double width;
	
	protected NodeEntity(CustomGraph graph, Node node) {
		this.index = node.index();
		NodeRealizer nRealizer = graph.getRealizer(node);
		nRealizer.getFillColor();
		this.x = nRealizer.getX();
		this.y = nRealizer.getY();
		this.height = nRealizer.getHeight();
		this.width = nRealizer.getWidth();
	}
	
	protected int getIndex() {
		return index;
	}

	protected void setIndex(int index) {
		this.index = index;
	}

	protected double getX() {
		return x;
	}

	protected void setX(double x) {
		this.x = x;
	}

	protected double getY() {
		return y;
	}

	protected void setY(double y) {
		this.y = y;
	}

	protected double getHeight() {
		return height;
	}

	protected void setHeight(double height) {
		this.height = height;
	}

	protected double getWidth() {
		return width;
	}

	protected void setWidth(double width) {
		this.width = width;
	}

	protected void createNode(CustomGraph graph) {
		Node node = graph.createNode();
		NodeRealizer nRealizer = graph.getRealizer(node);
		nRealizer.setX(x);
		nRealizer.setY(y);
		nRealizer.setHeight(height);
		nRealizer.setWidth(width);
	}
	
}
