package i5.las2peer.services.ocd.graphs;

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
 * @author Sebastian
 *
 */
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
	//ArangoDB
	public static final String graphKeyColumnName = "GRAPH_KEY";
	public static final String collectionName = "customedge";

	/**
	 * System generated persistence id.
	 */
	private int id;
	
	/**
	 * System generated persistence key.
	 */
	public String key;
	
	/**
	 * The graph that the edge belongs to.
	 */
	private CustomGraph graph;
	
	/**
	 * The edge weight.
	 */

	private double weight = 1;
	
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////// The following attributes are only of internal use for persistence purposes.
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	 * The source custom node.
	 * Only for persistence purposes.
	 */
	private CustomNode source;
	
	/*
	 * The target custom node.
	 * Only for persistence purposes.
	 */
	private CustomNode target;
	
//	/*
//	 * The points of the visual edge layout.
//	 * Only for persistence purposes.
//	 */
//
//	private List<PointEntity> points;
	
	
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
	 * @param customEdge The custom edge to copy.
	 */
	protected CustomEdge(CustomEdge customEdge) {
		this.weight = customEdge.weight;
	}
	
	/**
	 * Getter for the id.
	 * @return The id.
	 */
	public int getId() {
		return id;
	}
	public CustomGraph getGraph() {
		return this.graph;
	}

	public void setGraph(CustomGraph graph) {
		this.graph = graph;
	}

	/**
	 * Getter for the key.
	 * @return The key.
	 */
	public String getKey() {
		return this.key;
	}	
	
	/**
	 * Getter for the edge weight.
	 * @return The edge weight.
	 */
	protected double getWeight() {
		return weight;
	}

	/**
	 * Setter for the edge weight.
	 * @param weight The edge weight.
	 */
	protected void setWeight(double weight) {
		this.weight = weight;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	/////////// The following methods are only of internal use for persistence purposes.
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Getter for the source node.
	 * Only for persistence purposes.
	 * @return The source custom node.
	 */
	protected CustomNode getSource() {
		return source;
	}

	/**
	 * Setter for the source node.
	 * Only for persistence purposes.
	 * @param source The source custom node.
	 */
	protected void setSource(CustomNode source) {
		this.source = source;
	}
	
	/**
	 * Getter for the target node.
	 * Only for persistence purposes.
	 * @return The target custom node.
	 */
	protected CustomNode getTarget() {
		return target;
	}

	/**
	 * Setter for the target node.
	 * Only for persistence purposes.
	 * @param target The target custom node.
	 */
	protected void setTarget(CustomNode target) {
		this.target = target;
	}
	
//	/*
//	 * Getter for the points of the visual edge layout.
//	 * Only for persistence purposes.
//	 * @return The points.
//	 */
//	protected List<PointEntity> getPoints() {
//		return points;
//	}
//
//	/*
//	 * Setter for the points of the visual edge layout.
//	 * Only for persistence purposes.
//	 * @param points The points.
//	 */
//	protected void setPoints(List<PointEntity> points) {
//		this.points = points;
//	}
	
	/**
	 * Updates a custom edge before it is being persistence.
	 * Only for persistence purposes.
	 * @param graph The graph that the edge is part of.
	 * @param edge The corresponding yFiles edge.
	 */
	protected void update(CustomGraph graph, Edge edge) {
		this.source = graph.getCustomNode(edge.getSourceNode());
		this.target = graph.getCustomNode(edge.getTargetNode());
//		EdgeRealizer eRealizer = graph.getRealizer(edge);
//		this.points = new ArrayList<PointEntity>();
//		this.points.add(new PointEntity(eRealizer.getSourcePoint()));
//		this.points.add(new PointEntity(eRealizer.getTargetPoint()));
//		for(int i=0; i<eRealizer.pointCount(); i++) {
//			this.points.add(new PointEntity(eRealizer.getPoint(i)));
//		}
		this.graph = graph;
	}

	/**
	 * Creates the corresponding graphstream edge after the custom edge is loaded from persistence.
	 * Only for persistence purposes.
	 * @param graph The graph that the edge is part of.
	 * @param source The source node of the edge.
	 * @param target The target node of the edge.
	 * @return The edge.
	 */
	protected Edge createEdge(CustomGraph graph, Node source, Node target) {
		//TODO: Again figure out how to name edges
		Edge edge = graph.addEdge(UUID.randomUUID().toString(), source, target); 
//		EdgeRealizer eRealizer = graph.getRealizer(edge);
//		eRealizer.setSourcePoint(points.get(0).createPoint());
//		eRealizer.setTargetPoint(points.get(1).createPoint());
//		for(int i=2; i<points.size(); i++) {
//			PointEntity point = points.get(i);
//			eRealizer.addPoint(point.getX(), point.getY());;
//		}
		return edge;
	}
	
	
	//persistence functions
	public void persist(ArangoDatabase db, DocumentCreateOptions opt) {
		ArangoCollection collection = db.collection(collectionName);
		BaseEdgeDocument bed = new BaseEdgeDocument();
		bed.addAttribute(weightColumnName, this.weight);
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
		bed.addAttribute(graphKeyColumnName, this.graph.getKey());
		bed.setFrom(CustomNode.collectionName + "/" + this.source.getKey());
		bed.setTo(CustomNode.collectionName + "/" + this.target.getKey());
		collection.updateDocument(this.key, bed, opt);
	}
	
	public static CustomEdge load(BaseEdgeDocument bed, CustomNode source, CustomNode target, CustomGraph graph, ArangoDatabase db) {
		CustomEdge ce = new CustomEdge();
		if (bed != null) {
			ce.key = bed.getKey();
			ce.graph = graph;
			if(bed.getAttribute(weightColumnName)!=null) {
				ce.weight = Double.parseDouble(bed.getAttribute(weightColumnName).toString());
			}
			ce.source = source;
			ce.target = target;
		}	
		else {
			System.out.println("Empty Document");
		}
		return ce;
	}
	
	
	
	
	public String String() {
		String n = System.getProperty("line.separator");
		String ret = "CustomNode: " + n;
		ret += "Key :           " + this.key + n;
		ret += "weight :        " + this.weight;
		ret += "source Key :    " + this.source.getKey();
		ret += "target Key :    " + this.target.getKey();
		
		return ret;
	}
}
