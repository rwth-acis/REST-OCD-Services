package i5.las2peer.services.ocd.graphs;

import javax.persistence.*;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.CustomNodeId;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiNode;
import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentUpdateOptions;

import com.fasterxml.jackson.databind.ObjectMapper;

import i5.las2peer.services.ocd.metrics.OcdMetricLog;

import java.util.Map;


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
	public static final String idColumnName = "INDEX";
	public static final String graphIdColumnName = "GRAPH_ID";
	public static final String graphUserColumnName = "USER_NAME";
	public static final String nameColumnName = "NAME";
	public static final String extraInfoColumnName = "EXTRA_INFO";
	
	public static final String graphKeyColumnName = "GRAPH_KEY";
	public static final String collectionName = "customnode";
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
	 * System generated persistence key.
	 */
	private String key;
	
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

	@ElementCollection
	private JSONObject extraInfo = new JSONObject();
	
			
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
	 * Getter for the key.
	 * @return The key.
	 */
	public String getKey() {
		return this.key;
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

	/**
	 * Setter for the nodes extra Information string.
	 *
	 * @param extraInfo the String to fill the extra information attribute
	 */
	public void setExtraInfo(JSONObject extraInfo) {
		this.extraInfo = extraInfo;
	}

	/**
	 * Getter for the nodes extra Information string.
	 *
	 * @return The nodes extra Information string.
	 */
	public JSONObject getExtraInfo() {
		return extraInfo;
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
		//TODO: Check whether it made sense to replace this here but the previous createNode definitely also didnt seem right as this doesnt even add a custom node
		Node node = graph.addNode(this.name != null ? this.name : this.getKey());
//		NodeRealizer nRealizer = graph.getRealizer(node);
//		nRealizer.setX(this.x);
//		nRealizer.setY(this.y);
//		nRealizer.setHeight(this.height);
//		nRealizer.setWidth(this.width);
//		nRealizer.setFillColor(new Color(this.color));
		return node;
	}
	
	//persistence functions
	public void persist(ArangoDatabase db, DocumentCreateOptions opt) {
		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		bd.addAttribute(nameColumnName, this.name);
		bd.addAttribute(extraInfoColumnName, this.extraInfo);
		bd.addAttribute(graphKeyColumnName, this.graph.getKey());
		
		collection.insertDocument(bd, opt);
		this.key = bd.getKey();
	}
	
	public static CustomNode load(BaseDocument bd, CustomGraph graph) throws OcdPersistenceLoadException {
		CustomNode cn = new CustomNode();
		if (bd != null) {
			cn.key = bd.getKey();
			cn.graph = graph;
			if(bd.getAttribute(nameColumnName)!= null) {
				cn.name = bd.getAttribute(nameColumnName).toString();
			}
			if(bd.getAttribute(extraInfoColumnName) != null){
				try {
					ObjectMapper om = new ObjectMapper();
					cn.extraInfo = new JSONObject(om.convertValue(bd.getAttribute(extraInfoColumnName), Map.class));
				}
				catch (Exception e) {
					throw new OcdPersistenceLoadException("Could not parse extraInfo of Node. " + e.getMessage());
				}
			}
			else {
				cn.extraInfo = new JSONObject();
			}

		}	
		else {
			System.out.println("Empty Document");
		}
		return cn;
	}
	
	public void updateDB(ArangoDatabase db, DocumentUpdateOptions opt) throws ParseException {
		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		bd.addAttribute(nameColumnName,  this.name);
		bd.addAttribute(graphKeyColumnName, this.graph.getKey());
		bd.addAttribute(extraInfoColumnName, this.extraInfo);

		collection.updateDocument(this.key, bd, opt);
	}
	
	//TODO wird die funktion gebraucht?
	public static CustomNode load(String key, CustomGraph graph, ArangoDatabase db, DocumentReadOptions opt) throws ParseException {
		CustomNode cn = new CustomNode();
		ArangoCollection collection = db.collection(collectionName);
		
		BaseDocument bd = collection.getDocument(key, BaseDocument.class, opt);
		if (bd != null) {
			String name = bd.getAttribute(nameColumnName).toString();
			
			cn.key = key;
			cn.graph = graph;
			if(bd.getAttribute(nameColumnName)!= null) {
				cn.name = bd.getAttribute(nameColumnName).toString();
			}
			if(bd.getAttribute(extraInfoColumnName) != null){
				JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
				cn.extraInfo = (JSONObject)parser.parse(bd.getAttribute(extraInfoColumnName).toString());
			}
			else {
				cn.extraInfo = new JSONObject();
			}
		}	
		else {
			System.out.println("Empty CustomNode Document");
		}
		return cn;
	}
	
	
	public String String() {
		String n = System.getProperty("line.separator");
		String ret = "CustomNode: " + n;
		ret += "Key :           " + this.key + n;
		ret += "name :         " + this.name +n;
		
		return ret;
	}

	@Override
	public String toString() {
		return "CustomNode{" +
				"id=" + id +
				", key='" + key + '\'' +
				", graph=" + graph +
				", name='" + name + '\'' +
				'}';
	}
}
