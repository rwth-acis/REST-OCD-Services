package i5.las2peer.services.servicePackage.graph;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import y.base.Node;

@Entity
public class Community {
	
	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String nameColumnName = "NAME";
	private static final String colorColumnName = "COLOR";
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = idColumnName)
	private long id;
	@Column(name = nameColumnName)
	/**
	 * The community name.
	 */
	private String name = "";
	/**
	 * The default color of community nodes, defined by the sRGB color model.
	 */
	@Column(name = colorColumnName)
	private int color = Color.WHITE.getRGB();
	/**
	 * A mapping from the custom nodes to their belonging factors.
	 */
	@ElementCollection
	private Map<CustomNode, Double> memberships = new HashMap<CustomNode, Double>();
	/**
	 * Getter for id.
	 * @return The id.
	 */
	public long getId() {
		return id;
	}
	/**
	 * Getter for name.
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * Setter for name.
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Getter for color.
	 * @return The color.
	 */
	public Color getColor() {
		return new Color(this.color);
	}
	/**
	 * Setter for color.
	 * @param color The color.
	 */
	public void setColor(Color color) {
		this.color = color.getRGB();
	}
	/**
	 * Getter for memberships.
	 * @param graph The graph of the corresponding cover.
	 * @param reverseNodeMap A reverse node mapping from the corresponding graph's custom nodes
	 * to the actual nodes.
	 * @return The memberships.
	 */
	public Map<Node, Double> getMemberships(CustomGraph graph) {
		Map<Node, Double> memberships = new HashMap<Node, Double> ();
		for(Map.Entry<CustomNode, Double> entry : this.memberships.entrySet()) {
			memberships.put(graph.getNode(entry.getKey()), entry.getValue());
		}
		return memberships;
	}
	/**
	 * Setter for memberships.
	 * @param graph The graph of the corresponding cover.
	 * @param memberships The memberships.
	 */
	public void setMemberships(CustomGraph graph, Map<Node, Double> memberships) {
		this.memberships = new HashMap<CustomNode, Double>();
		if(memberships != null) {
			for(Map.Entry<Node, Double> entry : memberships.entrySet()) {
				this.memberships.put(graph.getCustomNode(entry.getKey()), entry.getValue());
			}
		}
	}
	/**
	 * Setter for a membership entry.
	 * @param graph The graph of the corresponding cover.
	 * @param customNode The member custom node.
	 * @param belongingFactor The belonging factor.
	 */
	public void setBelongingFactor(CustomGraph graph, Node node, double belongingFactor) {
		CustomNode customNode = graph.getCustomNode(node);
		if(belongingFactor != 0) {
			this.memberships.put(customNode, belongingFactor);
		}
		else
			this.memberships.remove(customNode);
	}
	/**
	 * Getter for the belonging factor of a certain node.
	 * @param graph The graph of the corresponding cover.
	 * @param customNode The member custom node.
	 * @return The belonging factor, i.e. the corresponding value from the
	 * memberships map.
	 */
	public double getBelongingFactor(CustomGraph graph, Node node) {
		CustomNode customNode = graph.getCustomNode(node);
		Double belongingFactor = this.memberships.get(customNode);
		if(belongingFactor == null) {
			belongingFactor = 0d;
		}
		return belongingFactor;
	}
	/**
	 * Returns the community size, i.e. the amount of community members.
	 * @return
	 */
	public int getSize() {
		return this.memberships.size();
	}
	
}
