package i5.las2peer.services.servicePackage.graph;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import y.base.Edge;
import y.base.Node;
import y.view.EdgeRealizer;

@Entity
public class CustomEdge {
	
	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String sourceIndexColumnName = "SOURCE_INDEX";
	private static final String targetIndexColumnName = "TARGET_INDEX";
	private static final String weightColumnName = "WEIGHT";	
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = idColumnName)
	private int id;
	
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = sourceIndexColumnName, referencedColumnName = CustomNode.idColumnName)
	private CustomNode source;
	
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = targetIndexColumnName, referencedColumnName = CustomNode.idColumnName)
	private CustomNode target;
	
	@Column(name = weightColumnName)
	private double weight;
	
	@ElementCollection
	private List<PointEntity> points;
	
	protected CustomEdge() {
	}

	public int getId() {
		return id;
	}

	protected CustomNode getSource() {
		return source;
	}

	protected void setSource(CustomNode source) {
		this.source = source;
	}

	protected CustomNode getTarget() {
		return target;
	}

	protected void setTarget(CustomNode target) {
		this.target = target;
	}

	protected double getWeight() {
		return weight;
	}

	protected void setWeight(double weight) {
		this.weight = weight;
	}

	protected List<PointEntity> getPoints() {
		return points;
	}

	protected void setPoints(List<PointEntity> points) {
		this.points = points;
	}
	
	protected void update(CustomGraph graph, Edge edge) {
		this.source = graph.getCustomNode(edge.source());
		this.target = graph.getCustomNode(edge.target());
		EdgeRealizer eRealizer = graph.getRealizer(edge);
		this.points = new ArrayList<PointEntity>();
		this.points.add(new PointEntity(eRealizer.getSourcePoint()));
		this.points.add(new PointEntity(eRealizer.getTargetPoint()));
		for(int i=0; i<eRealizer.pointCount(); i++) {
			this.points.add(new PointEntity(eRealizer.getPoint(i)));
		}
	}

	protected Edge createEdge(CustomGraph graph, Node source, Node target) {
		Edge edge = graph.createEdge(source, target);
		EdgeRealizer eRealizer = graph.getRealizer(edge);
		eRealizer.setSourcePoint(points.get(0).createPoint());
		eRealizer.setTargetPoint(points.get(1).createPoint());
		for(int i=2; i<points.size(); i++) {
			PointEntity point = points.get(i);
			eRealizer.addPoint(point.getX(), point.getY());;
		}
		return edge;
	}
}
