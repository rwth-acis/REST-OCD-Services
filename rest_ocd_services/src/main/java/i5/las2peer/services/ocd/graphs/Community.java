package i5.las2peer.services.ocd.graphs;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import i5.las2peer.services.ocd.graphs.properties.GraphProperty;
import org.graphstream.graph.Node;

/**
 * Represents a community of a cover.
 * 
 * @author Sebastian
 *
 */


public class Community {

	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String nameColumnName = "NAME";
	private static final String colorColumnName = "COLOR";
	private static final String propertiesColumnName = "PROPERTIES";
	public static final String coverIdColumnName = "COVER_ID";
	public static final String graphIdColumnName = "GRAPH_ID";
	public static final String graphUserColumnName = "USER_NAME";
	private static final String membershipMapGraphIdKeyColumnName = "GRAPH_ID";
	private static final String membershipMapGraphUserKeyColumnName = "USER_NAME";
	private static final String membershipMapNodeIdKeyColumnName = "CUSTOM_NODE_ID";
	
	public static final String collectionName = "community";
	private static final String coverKeyColumnName = "COVER_KEY";
	private static final String membershipKeyMapColumnName = "MEMBERSHIP_KEYS";
	/**
	 * System generated persistence id.
	 */


	private long id;
	/**
	 * System generated persistence key.
	 */
	private String key;
	/**
	 * The cover that the community is part of.
	 */


	private Cover cover;

	/**
	 * The community name.
	 */

	private String name = "";

	/**
	 * The default color of community nodes, defined by the sRGB color model.
	 */

	private int color = Color.WHITE.getRGB();

	/**
	 * The communities properties.
	 */


	private List<Double> properties;

	/**
	 * A mapping from the community member (custom) nodes to their belonging
	 * factors. Belonging factors must be non-negative.
	 */
	private Map<CustomNode, Double> memberships = new HashMap<CustomNode, Double>();

	/**
	 * Creates a new instance.
	 * 
	 * @param cover
	 *            The cover the community belongs to.
	 */
	public Community(Cover cover) {
		this.cover = cover;
	}

	/**
	 * Creates a new community. Only for persistence purposes.
	 */
	protected Community() {
	}

	/**
	 * Getter for id.
	 * 
	 * @return The id.
	 */
	public long getId() {
		return id;
	}
	/**
	 * Getter for key.
	 * @return The key.
	 */
	public String getKey() {
		return key;
	}
	/**
	 * Getter for name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for name.
	 * 
	 * @param name
	 *            The name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for color.
	 * 
	 * @return The color.
	 */
	public Color getColor() {
		return new Color(this.color);
	}

	/**
	 * Setter for color.
	 * 
	 * @param color
	 *            The color.
	 */
	public void setColor(Color color) {
		this.color = color.getRGB();
	}

	/**
	 * Getter for cover.
	 * 
	 * @return the cover.
	 */
	public Cover getCover() {
		return this.cover;
	}

	/**
	 * Getter for memberships.
	 * 
	 * @return The memberships.
	 */
	public Map<Node, Double> getMemberships() {
		Map<Node, Double> memberships = new HashMap<Node, Double>();
		for (Map.Entry<CustomNode, Double> entry : this.memberships.entrySet()) {
			memberships.put(this.cover.getGraph().getNode(entry.getKey()), entry.getValue());
		}
		return memberships;
	}

	/**
	 * Getter for the belonging factor of a certain node.
	 * 
	 * @param node
	 *            The member node.
	 * @return The belonging factor, i.e. the corresponding value from the
	 *         memberships map or 0 if the node does not belong to the
	 *         community.
	 */
	public double getBelongingFactor(Node node) {
		CustomNode customNode = this.cover.getGraph().getCustomNode(node);
		Double belongingFactor = this.memberships.get(customNode);
		if (belongingFactor == null) {
			belongingFactor = 0d;
		}
		return belongingFactor;
	}

	/**
	 * Setter for a membership entry. If the belonging factor is 0 the node is
	 * removed from the community.
	 * 
	 * @param node
	 *            The member node.
	 * @param belongingFactor
	 *            The belonging factor.
	 */
	protected void setBelongingFactor(Node node, double belongingFactor) {
		CustomNode customNode = this.cover.getGraph().getCustomNode(node);
		if (belongingFactor != 0) {
			this.memberships.put(customNode, belongingFactor);
		} else
			this.memberships.remove(customNode);
	}

	/**
	 * Returns the community size, i.e. the amount of community members.
	 * 
	 * @return The size.
	 */
	public int getSize() {
		return this.memberships.size();
	}

	/**
	 * Return a specific property value
	 * 
	 * @param property
	 *            The GraphProperty
	 * @return the property value
	 */
	public double getProperty(GraphProperty property) {
		return properties.get(property.getId());
	}

	/**
	 * Sets the {@link #properties}. Should only be called by the community {@link Cover}
	 * or for test purposes.
	 * 
	 * @param properties
	 *            List of properties
	 */
	protected void setProperties(List<Double> properties) {
		this.properties = properties;
	}

	/**
	 * Returns the indices of all nodes that have a belonging to this community
	 * 
	 * @return member indices list
	 */
	public List<Integer> getMemberIndices() {
		List<Integer> memberIndices = new ArrayList<>();
		for (Map.Entry<CustomNode, Double> entry : this.memberships.entrySet()) {
			Node node = getCover().getGraph().getNode(entry.getKey());
			memberIndices.add(Integer.valueOf(getCover().getGraph().getNodeName(node)));
		}
		return memberIndices;
	}

	/////////////////////////// PERSISTENCE CALLBACK METHODS


	/*
	 * PreRemove Method. Removes all membership mappings.
	 */
	public void preRemove() {
		this.memberships.clear();
	}
	
	//persistence functions
	public void persist( ArangoDatabase db, DocumentCreateOptions opt) {
		ArangoCollection collection = db.collection(collectionName);
		BaseDocument bd = new BaseDocument();
		bd.addAttribute(nameColumnName, this.name);
		bd.addAttribute(colorColumnName, this.color);
		if(this.properties == null) {
			this.properties = new ArrayList<Double>();	//TODO kann das null bleiben?
		}
		bd.addAttribute(propertiesColumnName, this.properties);  
		bd.addAttribute(coverKeyColumnName, this.cover.getKey());
		Map<String, Double> membershipKeyMap = new HashMap<String, Double>();
		
		for (Map.Entry<CustomNode, Double> entry : this.memberships.entrySet()) {	
			membershipKeyMap.put(entry.getKey().getKey(), entry.getValue());	//CustomNode Keys muessen bekannt sein
		}
		bd.addAttribute(membershipKeyMapColumnName,  membershipKeyMap);
		collection.insertDocument(bd, opt);
		this.key = bd.getKey();
	}
	
	public static Community load(String key, Cover cover, ArangoDatabase db, DocumentReadOptions opt) {
		Community c = new Community();
		ArangoCollection collection = db.collection(collectionName);
		
		BaseDocument bd = collection.getDocument(key, BaseDocument.class, opt);
		if (bd != null) {
			ObjectMapper om = new ObjectMapper(); 
			String colorString = bd.getAttribute(colorColumnName).toString();
			Object objProperties = bd.getAttribute(propertiesColumnName);
			Object objMembershipKeyMap = bd.getAttribute(membershipKeyMapColumnName);
			HashMap<String, Double> membershipKeyMap = om.convertValue(objMembershipKeyMap,new TypeReference<HashMap<String,Double>>() { });

			c.key = key;
			c.cover = cover;
			c.name = bd.getAttribute(nameColumnName).toString();
			c.color = Integer.parseInt(colorString);
			c.properties = om.convertValue(objProperties, List.class);

			// each customNode is assigned the stored belongingValue
			for (Map.Entry<String, Double> entry : membershipKeyMap.entrySet()) {
				String nodeKey = entry.getKey();
				CustomNode cn = cover.getGraph().getCustomNodeByKey(nodeKey);// null fall abfangen
				c.memberships.put(cn, entry.getValue());
			}
		}	
		else {
			System.out.println("empty Community document");
		}
		return c;
	}
	
	
	public String String() {
		String n = System.getProperty("line.separator");
		String ret = "Community : " + n;
		if(this.cover != null) {ret += "cover : existiert" +n;}
		ret += "Key :           " + this.key + n;
		ret += "name :          " + this.name + n; 
		ret += "color value:    " + this.color + n;
		ret += "properties :    " + this.properties + n;
		if(this.memberships != null) {	
			for (Map.Entry<CustomNode, Double> entry : this.memberships.entrySet()) {
				CustomNode cn = entry.getKey();
				ret += cn.String() + entry.getValue() +n;
			}
		}
		return ret;
	}	

}
