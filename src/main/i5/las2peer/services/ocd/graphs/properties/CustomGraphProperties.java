package i5.las2peer.services.ocd.graphs.properties;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;

import i5.las2peer.services.ocd.graphs.CustomGraph;

/**
 * This class can compute and store the properties of a graph data structure.
 * The computations are divided into two methods.Calculate methods hold the
 * algorithm with primitive parameters. Initialize methods transform a data
 * structure into primitives to call the calculate methods.
 */
@Embeddable
public class CustomGraphProperties {

	///// Entity Fields /////

	@Basic
	private int nodes;

	@Basic
	private int edges;

	@ElementCollection
	private List<Double> properties;

	///// Methods /////

	public CustomGraphProperties() {
		
	}
	
	public CustomGraphProperties(CustomGraph graph) {
		initialize(graph);
	}

	public int getNodes() {
		return nodes;
	}

	public int getEdges() {
		return edges;
	}

	public List<Double> getProperties() {
		return properties;
	}

	public void setNodes(int nodes) {
		this.nodes = nodes;
	}

	public void setEdges(int edges) {
		this.edges = edges;
	}

	public void setProperties(List<Double> properties) {
		this.properties = properties;
	}

	public double getProperty(GraphProperty property) {

		return getProperties().get(property.getId());
	}

	protected void initialize(CustomGraph graph) {

		setNodes(graph.nodeCount());
		setEdges(graph.edgeCount());

		this.properties = new ArrayList<>(GraphProperty.size());
		for (int i = 0; i < GraphProperty.size(); i++) {
			CustomGraphProperty property = null;
			try {
				property = GraphProperty.lookupProperty(i).getPropertyClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
			this.properties.add(i, property.calculate(graph));
		}
	}

}
