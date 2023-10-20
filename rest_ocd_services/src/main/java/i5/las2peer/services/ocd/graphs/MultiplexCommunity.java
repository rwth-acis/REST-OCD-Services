package i5.las2peer.services.ocd.graphs;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.MapKeyJoinColumns;

import org.graphstream.graph.Node;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
public class MultiplexCommunity extends Community {

	public static final String multiplexCollectionName = "multiplexcommunity";
	private static final String membershipMapGraphIdKeyColumnName = "GRAPH_ID";
	private static final String membershipMapGraphUserKeyColumnName = "USER_NAME";
	private static final String membershipMapNodeIdKeyColumnName = "CUSTOM_NODE_ID";

	private static final String nameColumnName = "NAME";
	private static final String colorColumnName = "COLOR";
	private static final String propertiesColumnName = "PROPERTIES";
	public static final String coverIdColumnName = "COVER_ID";
	public static final String graphIdColumnName = "GRAPH_ID";
	public static final String graphUserColumnName = "USER_NAME";

	public static final String collectionName = "community";
	private static final String coverKeyColumnName = "COVER_KEY";
	private static final String membershipKeyMapColumnName = "MEMBERSHIP_KEYS";
	private static final String multiplexMembershipKeyMapColumnName = "MULTIPLEX_MEMBERSHIP_KEYS";

	@ElementCollection(fetch = FetchType.LAZY)
	@MapKeyJoinColumns({
			@MapKeyJoinColumn(name = membershipMapNodeIdKeyColumnName, referencedColumnName = CustomNode.idColumnName),
			@MapKeyJoinColumn(name = membershipMapGraphIdKeyColumnName, referencedColumnName = CustomNode.graphIdColumnName),
			@MapKeyJoinColumn(name = membershipMapGraphUserKeyColumnName, referencedColumnName = CustomNode.graphUserColumnName) })

	private Map<String, Map<CustomNode, Double>> multiplexMemberships = new HashMap<>();

	public MultiplexCommunity(Cover cover) {
		setCover(cover);
	}

	public MultiplexCommunity() {

	}

	/**
	 * Setter for multiplexMemberships.
	 * 
	 * @param multiplexMemberships
	 *                             The multiplexMemberships.
	 */
	public void setMultiplexMemberships(Map<String, Map<CustomNode, Double>> multiplexMemberships) {
		this.multiplexMemberships = multiplexMemberships;
	}
    

	/**
	 * Setter for belongingfactor of a node on a layer.
	 * @param layerId  The layerId the node is on.
	 * @param node     The node 
	 * @param belongingFactor  The belongingFactor of the node to this community.
	 */
	public void setMultiplexBelongingFactor(String layerId, Node node, Double belongingFactor) {
		CustomNode customNode = getCover().getGraph().getCustomNode(node);
		if (multiplexMemberships.get(layerId) == null) {
			multiplexMemberships.put(layerId, new HashMap<>());
		}
		if (belongingFactor != 0) {
			this.multiplexMemberships.get(layerId).put(customNode, belongingFactor);
		} else
			this.multiplexMemberships.get(layerId).remove(customNode);
	}

	/**
	 * Getter for MultiplexMemberships.
	 * 
	 * @return The MultiplexMemberships.
	 */
	public Map<String, Map<Node, Double>> getMultiplexMemberships() {
		Map<String, Map<Node, Double>> result = new HashMap<>();
		for (Map.Entry<String, Map<CustomNode, Double>> entry : this.multiplexMemberships.entrySet()) {
			result.put(entry.getKey(), new HashMap<>());
			for (Map.Entry<CustomNode, Double> entry2 : entry.getValue().entrySet()) {
				result.get(entry.getKey()).put(this.getCover().getGraph().getNode(entry2.getKey()), entry2.getValue());
			}
		}
		return result;
	}
    

	/**
	 * Getter for the belongingfactor of a node on a layer.
	 * 
	 * @return The belonging factor
	 */
	public double getMultiplexBelongingFactor(String layer, Node node) {
		CustomNode customNode = this.getCover().getGraph().getCustomNode(node);
		if (multiplexMemberships.containsKey(layer)) {
			if (multiplexMemberships.get(layer).containsKey(customNode)) {
				return multiplexMemberships.get(layer).get(customNode);
			}
		}
		return 0d;
	}

	// persistence functions
	@Override
	public void persist(ArangoDatabase db, DocumentCreateOptions opt) {
		ArangoCollection collection = db.collection(multiplexCollectionName);
		BaseDocument bd = new BaseDocument();
		bd.addAttribute(nameColumnName, getName());
		bd.addAttribute(colorColumnName, getPersistenceColor());
		if (getProperties() == null) {
			setProperties(new ArrayList<Double>()); 
		}
		bd.addAttribute(propertiesColumnName, getProperties());
		bd.addAttribute(coverKeyColumnName, getCover().getKey());

		Map<String, Map<String, Double>> multiplexMembershipKeyMap = new HashMap<>();

		for (Map.Entry<String, Map<CustomNode, Double>> entry : this.multiplexMemberships.entrySet()) {
			Map<String, Double> membershipKeyMap = new HashMap<>();
			for (Map.Entry<CustomNode, Double> entry2 : entry.getValue().entrySet()) {
				membershipKeyMap.put(entry2.getKey().getKey(), entry2.getValue()); // CustomNode Keys muessen bekannt
																					// sein
			}
			multiplexMembershipKeyMap.put(entry.getKey(), membershipKeyMap);
		}

		Map<String, Double> membershipKeyMap = new HashMap<String, Double>();

		for (Map.Entry<CustomNode, Double> entry : getMemberships2().entrySet()) {
			membershipKeyMap.put(entry.getKey().getKey(), entry.getValue()); // CustomNode Keys muessen bekannt sein
		}

		bd.addAttribute(membershipKeyMapColumnName, membershipKeyMap);
		bd.addAttribute(multiplexMembershipKeyMapColumnName, multiplexMembershipKeyMap);
		collection.insertDocument(bd, opt);
		setKey(bd.getKey());
	}

	public static MultiplexCommunity load(String key, Cover cover, ArangoDatabase db, DocumentReadOptions opt) {
		MultiplexCommunity c = new MultiplexCommunity();
		ArangoCollection collection = db.collection(multiplexCollectionName);

		BaseDocument bd = collection.getDocument(key, BaseDocument.class, opt);
		if (bd != null) {
			ObjectMapper om = new ObjectMapper();
			String colorString = bd.getAttribute(colorColumnName).toString();
			Object objProperties = bd.getAttribute(propertiesColumnName);
			Object objMultiplexMembershipKeyMap = bd.getAttribute(multiplexMembershipKeyMapColumnName);
			HashMap<String, Map<String, Double>> multiplexMembershipKeyMap = om.convertValue(
					objMultiplexMembershipKeyMap,
					new TypeReference<HashMap<String, Map<String, Double>>>() {
					});

			Object objMembershipKeyMap = bd.getAttribute(membershipKeyMapColumnName);
			HashMap<String, Double> membershipKeyMap = om.convertValue(objMembershipKeyMap,
					new TypeReference<HashMap<String, Double>>() {
					});

			c.setKey(key);
			c.setCover(cover);
			c.setName(bd.getAttribute(nameColumnName).toString());
			c.setPersistenceColor(Integer.parseInt(colorString));
			c.setProperties(om.convertValue(objProperties, List.class));

			// each customNode is assigned the stored belongingValue
			for (Map.Entry<String, Map<String, Double>> entry : multiplexMembershipKeyMap.entrySet()) {

				Map<CustomNode, Double> multiplexMembershipKeyMaps = new HashMap<>();

				for (Map.Entry<String, Double> entry2 : entry.getValue().entrySet()) {

					String nodeKey = entry2.getKey();
					CustomNode cn = cover.getGraph().getCustomNodeByKey(nodeKey);// null fall abfangen
					multiplexMembershipKeyMaps.put(cn, entry2.getValue());
				}
				c.multiplexMemberships.put(entry.getKey(), multiplexMembershipKeyMaps);

			}

			// each customNode is assigned the stored belongingValue
			for (Map.Entry<String, Double> entry : membershipKeyMap.entrySet()) {
				String nodeKey = entry.getKey();
				CustomNode cn = cover.getGraph().getCustomNodeByKey(nodeKey);// null fall abfangen
				c.getMemberships2().put(cn, entry.getValue());
			}

		} else {
			System.out.println("empty Comumnity document");
		}
		return c;
	}

}
