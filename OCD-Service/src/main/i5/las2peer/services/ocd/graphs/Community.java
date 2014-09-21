package i5.las2peer.services.ocd.graphs;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

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
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.MapKeyJoinColumns;
import javax.persistence.PreRemove;

import y.base.Node;

/**
 * Represents a community of a cover.
 * @author Sebastian
 *
 */
@Entity
@IdClass(CommunityId.class)
public class Community {
	
	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String nameColumnName = "NAME";
	private static final String colorColumnName = "COLOR";
	public static final String coverIdColumnName = "COVER_ID";
	public static final String graphIdColumnName = "GRAPH_ID";
	public static final String graphUserColumnName = "USER_NAME";
	private static final String membershipMapGraphIdKeyColumnName = "GRAPH_ID";
	private static final String membershipMapGraphUserKeyColumnName = "USER_NAME";
	private static final String membershipMapNodeIdKeyColumnName = "CUSTOM_NODE_ID";
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = idColumnName)
	private long id;
	
	/**
	 * The cover that the community is part of.
	 */
	@Id
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name=graphIdColumnName, referencedColumnName=Cover.graphIdColumnName),
		@JoinColumn(name=graphUserColumnName, referencedColumnName=Cover.graphUserColumnName),
		@JoinColumn(name=coverIdColumnName, referencedColumnName=Cover.idColumnName)
	})
	private Cover cover;
	
	/**
	 * The community name.
	 */
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
	 * A mapping from the community member (custom) nodes to their belonging factors.
	 * Belonging factors must be non-negative.
	 */
	@ElementCollection(fetch=FetchType.LAZY)
	@MapKeyJoinColumns( {
		@MapKeyJoinColumn(name = membershipMapNodeIdKeyColumnName, referencedColumnName = CustomNode.idColumnName),
		@MapKeyJoinColumn(name = membershipMapGraphIdKeyColumnName, referencedColumnName = CustomNode.graphIdColumnName),
		@MapKeyJoinColumn(name = membershipMapGraphUserKeyColumnName, referencedColumnName = CustomNode.graphUserColumnName)
	} )
	private Map<CustomNode, Double> memberships = new HashMap<CustomNode, Double>();
	
	/**
	 * Creates a new instance.
	 * @param cover The cover the community belongs to.
	 */
	public Community(Cover cover) {
		this.cover = cover;
	}
	
	/**
	 * Creates a new community.
	 * Only for persistence purposes.
	 */
	protected Community() {
	}
	
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
	 * @return The memberships.
	 */
	public Map<Node, Double> getMemberships() {
		Map<Node, Double> memberships = new HashMap<Node, Double> ();
		for(Map.Entry<CustomNode, Double> entry : this.memberships.entrySet()) {
			memberships.put(this.cover.getGraph().getNode(entry.getKey()), entry.getValue());
		}
		return memberships;
	}
	/**
	 * Setter for a membership entry. If the belonging factor is 0 the node is removed from the community.
	 * @param node The member node.
	 * @param belongingFactor The belonging factor.
	 */
	protected void setBelongingFactor(Node node, double belongingFactor) {
		CustomNode customNode = this.cover.getGraph().getCustomNode(node);
		if(belongingFactor != 0) {
			this.memberships.put(customNode, belongingFactor);
		}
		else
			this.memberships.remove(customNode);
	}
	/**
	 * Getter for the belonging factor of a certain node.
	 * @param node The member node.
	 * @return The belonging factor, i.e. the corresponding value from the
	 * memberships map or 0 if the node does not belong to the community.
	 */
	public double getBelongingFactor(Node node) {
		CustomNode customNode = this.cover.getGraph().getCustomNode(node);
		Double belongingFactor = this.memberships.get(customNode);
		if(belongingFactor == null) {
			belongingFactor = 0d;
		}
		return belongingFactor;
	}
	/**
	 * Returns the community size, i.e. the amount of community members.
	 * @return The size.
	 */
	public int getSize() {
		return this.memberships.size();
	}
	
	/////////////////////////// PERSISTENCE CALLBACK METHODS
	
	/*
	 * PreRemove Method.
	 * Removes all membership mappings.
	 */
	@PreRemove
	public void preRemove() {
		this.memberships.clear();
	}
	
}
