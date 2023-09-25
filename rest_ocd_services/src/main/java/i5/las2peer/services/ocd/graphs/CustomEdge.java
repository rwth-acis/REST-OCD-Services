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

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoEdgeCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.arangodb.model.EdgeCreateOptions;

import java.util.UUID;
import org.graphstream.graph.implementations.AbstractEdge;
import org.graphstream.graph.Node;
import org.graphstream.graph.Edge;

/**
 * Custom edge extension.
 * Holds edge meta information and is used for edge persistence.
 * 
 * @author Sebastian
 *
 */
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
	private static final String layerIdColumnName = "LAYER_ID";
	// ArangoDB
	public static final String graphKeyColumnName = "GRAPH_KEY";
	public static final String collectionName = "customedge";
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = idColumnName)
	private int id;

	/**
	 * System generated persistence key.
	 */
	public String key;

	/**
	 * The graph that the edge belongs to.
	 */
	@Id
	@ManyToOne // (fetch=FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = graphIdColumnName, referencedColumnName = CustomGraph.idColumnName),
			@JoinColumn(name = graphUserColumnName, referencedColumnName = CustomGraph.userColumnName)
	})
	private CustomGraph graph;

	/**
	 * The edge weight.
	 */
	@Column(name = weightColumnName)
	private double weight = 1;

	/**
	 * The layer ID.
	 */
	@Column(name = layerIdColumnName)
	private String layerId = "";

	/////////////////////////////////////////////////////////////////////////////////////////
	/////////// The following attributes are only of internal use for persistence
	///////////////////////////////////////////////////////////////////////////////////////// purposes.
	/////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * The source custom node.
	 * Only for persistence purposes.
	 */
	@ManyToOne // (cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumns({
			@JoinColumn(name = sourceIndexColumnName, referencedColumnName = CustomNode.idColumnName),
			@JoinColumn(name = graphIdColumnName, referencedColumnName = CustomNode.graphIdColumnName, insertable = false, updatable = false),
			@JoinColumn(name = graphUserColumnName, referencedColumnName = CustomNode.graphUserColumnName, insertable = false, updatable = false)
	})
	private CustomNode source;

	/*
	 * The target custom node.
	 * Only for persistence purposes.
	 */
	@ManyToOne // (cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumns({
			@JoinColumn(name = targetIndexColumnName, referencedColumnName = CustomNode.idColumnName),
			@JoinColumn(name = graphIdColumnName, referencedColumnName = CustomNode.graphIdColumnName, insertable = false, updatable = false),
			@JoinColumn(name = graphUserColumnName, referencedColumnName = CustomNode.graphUserColumnName, insertable = false, updatable = false)
	})
	private CustomNode target;

	// /*
	// * The points of the visual edge layout.
	// * Only for persistence purposes.
	// */
	// @ElementCollection
	// private List<PointEntity> points;

	//////////////////////////////////////////////////////////////////
	//////// Methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance.
	 */
	protected CustomEdge() {
	}

	/**
	 * Copy constructor.
	 * 
	 * @param customEdge The custom edge to copy.
	 */
	protected CustomEdge(CustomEdge customEdge) {
		this.weight = customEdge.weight;
		this.layerId = customEdge.layerId;
	}

	/**
	 * Getter for the id.
	 * 
	 * @return The id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Getter for the key.
	 * 
	 * @return The key.
	 */
	public String getKey() {
		return this.key;
	}

	/**
	 * Getter for the edge weight.
	 * 
	 * @return The edge weight.
	 */
	protected double getWeight() {
		return weight;
	}

	/**
	 * Setter for the edge weight.
	 * 
	 * @param weight The edge weight.
	 */
	protected void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * Getter for the layerId.
	 * 
	 * @return The layerId.
	 */
	protected String getLayerId() {
		return layerId;
	}

	/**
	 * Setter for the layerId.
	 * 
	 * @param layerId The layerId.
	 */
	protected void setLayerId(String layerId) {
		this.layerId = layerId;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	/////////// The following methods are only of internal use for persistence
	///////////////////////////////////////////////////////////////////////////////////////// purposes.
	/////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Getter for the source node.
	 * Only for persistence purposes.
	 * 
	 * @return The source custom node.
	 */
	protected CustomNode getSource() {
		return source;
	}

	/**
	 * Setter for the source node.
	 * Only for persistence purposes.
	 * 
	 * @param source The source custom node.
	 */
	protected void setSource(CustomNode source) {
		this.source = source;
	}

	/**
	 * Getter for the target node.
	 * Only for persistence purposes.
	 * 
	 * @return The target custom node.
	 */
	protected CustomNode getTarget() {
		return target;
	}

	/**
	 * Setter for the target node.
	 * Only for persistence purposes.
	 * 
	 * @param target The target custom node.
	 */
	protected void setTarget(CustomNode target) {
		this.target = target;
	}

	// /*
	// * Getter for the points of the visual edge layout.
	// * Only for persistence purposes.
	// * @return The points.
	// */
	// protected List<PointEntity> getPoints() {
	// return points;
	// }
	//
	// /*
	// * Setter for the points of the visual edge layout.
	// * Only for persistence purposes.
	// * @param points The points.
	// */
	// protected void setPoints(List<PointEntity> points) {
	// this.points = points;
	// }

	/**
	 * Updates a custom edge before it is being persistence.
	 * Only for persistence purposes.
	 * 
	 * @param graph The graph that the edge is part of.
	 * @param edge  The corresponding yFiles edge.
	 */
	protected void update(CustomGraph graph, Edge edge) {
		this.source = graph.getCustomNode(edge.getSourceNode());
		this.target = graph.getCustomNode(edge.getTargetNode());
		// EdgeRealizer eRealizer = graph.getRealizer(edge);
		// this.points = new ArrayList<PointEntity>();
		// this.points.add(new PointEntity(eRealizer.getSourcePoint()));
		// this.points.add(new PointEntity(eRealizer.getTargetPoint()));
		// for(int i=0; i<eRealizer.pointCount(); i++) {
		// this.points.add(new PointEntity(eRealizer.getPoint(i)));
		// }
		this.graph = graph;
	}

	/**
	 * Creates the corresponding graphstream edge after the custom edge is loaded
	 * from persistence.
	 * Only for persistence purposes.
	 * 
	 * @param graph  The graph that the edge is part of.
	 * @param source The source node of the edge.
	 * @param target The target node of the edge.
	 * @return The edge.
	 */
	protected Edge createEdge(CustomGraph graph, Node source, Node target) {
		// TODO: Again figure out how to name edges
		Edge edge = graph.addEdge(UUID.randomUUID().toString(), source, target);
		// EdgeRealizer eRealizer = graph.getRealizer(edge);
		// eRealizer.setSourcePoint(points.get(0).createPoint());
		// eRealizer.setTargetPoint(points.get(1).createPoint());
		// for(int i=2; i<points.size(); i++) {
		// PointEntity point = points.get(i);
		// eRealizer.addPoint(point.getX(), point.getY());;
		// }
		return edge;
	}

	// persistence functions
	public void persist(ArangoDatabase db, DocumentCreateOptions opt) {
		ArangoCollection collection = db.collection(collectionName);
		BaseEdgeDocument bed = new BaseEdgeDocument();
		bed.addAttribute(weightColumnName, this.weight);
		bed.addAttribute(layerIdColumnName, this.layerId);
		bed.addAttribute(graphKeyColumnName, this.graph.getKey());
		bed.setFrom(CustomNode.collectionName + "/" + this.source.getKey());
		bed.setTo(CustomNode.collectionName + "/" + this.target.getKey());

		collection.insertDocument(bed, opt);
		this.key = bed.getKey();
	}

	public void updateDB(ArangoDatabase db, DocumentUpdateOptions opt) {

		ArangoCollection collection = db.collection(collectionName);
		BaseEdgeDocument bed = new BaseEdgeDocument();
		bed.addAttribute(weightColumnName, this.weight);
		bed.addAttribute(layerIdColumnName, this.layerId);
		bed.addAttribute(graphKeyColumnName, this.graph.getKey());
		bed.setFrom(CustomNode.collectionName + "/" + this.source.getKey());
		bed.setTo(CustomNode.collectionName + "/" + this.target.getKey());
		collection.updateDocument(this.key, bed, opt);
	}

	public static CustomEdge load(BaseEdgeDocument bed, CustomNode source, CustomNode target, CustomGraph graph,
			ArangoDatabase db) {
		CustomEdge ce = new CustomEdge();
		if (bed != null) {
			ce.key = bed.getKey();
			ce.graph = graph;
			if (bed.getAttribute(weightColumnName) != null) {
				ce.weight = Double.parseDouble(bed.getAttribute(weightColumnName).toString());
			}
			if (bed.getAttribute(layerIdColumnName) != null) {
				ce.layerId = bed.getAttribute(layerIdColumnName).toString();
			}
			ce.source = source;
			ce.target = target;
		} else {
			System.out.println("Empty Document");
		}
		return ce;
	}

	public String String() {
		String n = System.getProperty("line.separator");
		String ret = "CustomNode: " + n;
		ret += "Key :           " + this.key + n;
		ret += "weight :        " + this.weight;
		ret += "layerId :       " + this.layerId;
		ret += "source Key :    " + this.source.getKey();
		ret += "target Key :    " + this.target.getKey();

		return ret;
	}
}
