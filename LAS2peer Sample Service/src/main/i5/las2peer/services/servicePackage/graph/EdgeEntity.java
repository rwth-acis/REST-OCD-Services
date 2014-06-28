package i5.las2peer.services.servicePackage.graph;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import y.base.Edge;
import y.base.Node;
import y.view.EdgeRealizer;

@Embeddable
public class EdgeEntity {
	
	@Column
	private int index;
	
	@Embedded
	private NodeEntity source;
	
	@Embedded
	private NodeEntity target;
	
	@ElementCollection
	private List<PointEntity> points;
	
	@Embedded
	private PointEntity sourcePoint;
	
	@Embedded
	private PointEntity targetPoint;
	
	protected EdgeEntity(CustomGraph graph, Edge edge, NodeEntity source, NodeEntity target) {
		this.index = edge.index();
		this.source = source;
		this.target = target;
		EdgeRealizer eRealizer = graph.getRealizer(edge);
		for(int i=0; i<eRealizer.pointCount(); i++) {
			this.points.add(new PointEntity(eRealizer.getPoint(i)));
		}
		this.sourcePoint = new PointEntity(eRealizer.getSourcePoint());
		this.targetPoint = new PointEntity(eRealizer.getTargetPoint());
	}
	
	protected int getIndex() {
		return index;
	}



	protected void setIndex(int index) {
		this.index = index;
	}



	protected NodeEntity getSource() {
		return source;
	}



	protected void setSource(NodeEntity source) {
		this.source = source;
	}



	protected NodeEntity getTarget() {
		return target;
	}



	protected void setTarget(NodeEntity target) {
		this.target = target;
	}



	protected List<PointEntity> getPoints() {
		return points;
	}



	protected void setPoints(List<PointEntity> points) {
		this.points = points;
	}



	protected PointEntity getSourcePoint() {
		return sourcePoint;
	}



	protected void setSourcePoint(PointEntity sourcePoint) {
		this.sourcePoint = sourcePoint;
	}



	protected PointEntity getTargetPoint() {
		return targetPoint;
	}



	protected void setTargetPoint(PointEntity targetPoint) {
		this.targetPoint = targetPoint;
	}



	protected void createEdge(CustomGraph graph, Node source, Node target) {
		Edge edge = graph.createEdge(source, target);
		EdgeRealizer eRealizer = graph.getRealizer(edge);
		for(int i=0; i<points.size(); i++) {
			PointEntity point = points.get(i);
			eRealizer.addPoint(point.getX(), point.getY());;
		}
		eRealizer.setSourcePoint(sourcePoint.createPoint());
		eRealizer.setTargetPoint(targetPoint.createPoint());
	}
}
