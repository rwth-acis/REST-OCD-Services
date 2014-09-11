package i5.las2peer.services.ocd.graphs;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

import y.base.Edge;
import y.base.Node;
import y.view.EdgeRealizer;

@Entity
@IdClass(CustomEdgeId.class)
public class CustomEdge {
	
	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String sourceIndexColumnName = "SOURCE_INDEX";
	private static final String targetIndexColumnName = "TARGET_INDEX";
	protected static final String graphIdColumnName = "GRAPH_ID";
	protected static final String graphUserColumnName = "USER_NAME";
	
	private static final String weightColumnName = "WEIGHT";	
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = idColumnName)
	private int id;
	
	@Id
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name = graphIdColumnName, referencedColumnName = CustomGraph.idColumnName),
		@JoinColumn(name = graphUserColumnName, referencedColumnName = CustomGraph.userColumnName)
	})
	private CustomGraph graph;
	
	@Column(name = weightColumnName)
	private double weight = 1;
	
	/*
	 * Attributes from y.base.Edge
	 * Only for persistence.
	 */
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumns( {
		@JoinColumn(name = sourceIndexColumnName, referencedColumnName = CustomNode.idColumnName),
		@JoinColumn(name = graphIdColumnName, referencedColumnName = CustomNode.graphIdColumnName, insertable=false, updatable=false),
		@JoinColumn(name = graphUserColumnName, referencedColumnName = CustomNode.graphUserColumnName, insertable=false, updatable=false)
	} )
	private CustomNode source;
	
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumns( {
		@JoinColumn(name = targetIndexColumnName, referencedColumnName = CustomNode.idColumnName),
		@JoinColumn(name = graphIdColumnName, referencedColumnName = CustomNode.graphIdColumnName, insertable=false, updatable=false),
		@JoinColumn(name = graphUserColumnName, referencedColumnName = CustomNode.graphUserColumnName, insertable=false, updatable=false)
	} )
	private CustomNode target;
	
	@ElementCollection
	private List<PointEntity> points;
	
	protected CustomEdge() {
	}
	
	protected CustomEdge(CustomEdge customEdge) {
		this.weight = customEdge.weight;
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
		this.graph = graph;
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
