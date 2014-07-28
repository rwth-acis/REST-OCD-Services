package i5.las2peer.services.servicePackage.graph;

import i5.las2peer.services.servicePackage.algorithms.AlgorithmIdentifier;
import i5.las2peer.services.servicePackage.algorithms.AlgorithmLog;
import i5.las2peer.services.servicePackage.metrics.MetricIdentifier;
import i5.las2peer.services.servicePackage.metrics.MetricLog;
import i5.las2peer.services.servicePackage.utils.NonZeroEntriesVectorProcedure;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;

import y.base.Node;
import y.base.NodeCursor;


@Entity
public class Cover {

	/////////////////// DATABASE COLUMN NAMES
	
	/*
	 * Database column name definitions.
	 */
	private static final String graphIdColumnName = "GRAPH_ID";
	private static final String nameColumnName = "NAME";
	private static final String descriptionColumnName = "DESCRIPTION";
	private static final String idColumnName = "ID";
	private static final String idJoinColumnName = "COVER_ID";
	private static final String lastUpdateColumnName = "LAST_UPDATE";
	private static final String algorithmColumnName = "ALGORITHM";
	
	///////////////////////// ATTRIBUTES
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = idColumnName)
	private long id;
	/**
	 * The graph that the cover is based on.
	 */
	@ManyToOne
	@JoinColumn(name = graphIdColumnName, referencedColumnName = CustomGraph.idColumnName)
	private CustomGraph graph = new CustomGraph();	
	/**
	 * The name of the cover.
	 */
	@Column(name = nameColumnName)
	private String name = "";
	/**
	 * A description of the cover.
	 */
	@Column(name = descriptionColumnName)
	private String description = "";
	/**
	 * Last time of modification.
	 */
	@Version
	@Column(name = lastUpdateColumnName)
	private Timestamp lastUpdate;
	/**
	 * Logged data about the algorithm that created the cover.
	 */
	@OneToOne(orphanRemoval = true, cascade={CascadeType.ALL})
	@JoinColumn(name = algorithmColumnName)
	private AlgorithmLog algorithm = new AlgorithmLog(AlgorithmIdentifier.UNDEFINED, new HashMap<String, String>());
	@OneToMany(orphanRemoval = true, cascade={CascadeType.ALL})
	@JoinColumn(name=idJoinColumnName, referencedColumnName = idColumnName)
	private List<Community> communities = new ArrayList<Community>();
	@OneToMany(orphanRemoval = true, cascade={CascadeType.ALL})
	@JoinColumn(name=idJoinColumnName, referencedColumnName = idColumnName)
	private List<MetricLog> metrics = new ArrayList<MetricLog>();
	
	/////////////////////////////////////////// METHODS AND CONSTRUCTORS
	
	/*
	 * Only for persistence purposes.
	 */
	protected Cover() {
	}
	
	public Cover(CustomGraph graph, Matrix memberships, AlgorithmLog algorithm) {
		setGraph(graph);
		setMemberships(memberships);
		setAlgorithm(algorithm);
	}

	
	public long getId() {
		return id;
	}
	
	public CustomGraph getGraph() {
		return graph;
	}

	public void setGraph(CustomGraph graph) {
		this.graph = graph;
	}

	public Matrix getMemberships() {
		Matrix memberships = new CCSMatrix(graph.nodeCount(), communities.size());
		Map<CustomNode, Integer> customNodeIds = new HashMap<CustomNode, Integer>();
		NodeCursor nodes = graph.nodes();
		while(nodes.ok()) {
			Node node = nodes.node();
			customNodeIds.put(graph.getCustomNode(node), node.index());
			nodes.next();
		}
		for(int i=0; i<communities.size(); i++) {
			Community community = communities.get(i);
			for(Map.Entry<CustomNode, Double> membership : community.getMemberships().entrySet()) {
				memberships.set(customNodeIds.get(membership.getKey()), i, membership.getValue());
			}
		}
		return memberships;
	}

	public void setMemberships(Matrix memberships) {
		communities.clear();
		Node[] nodes = graph.getNodeArray();
		for(int j=0; j<memberships.columns(); j++) {
			Community community = new Community();
			communities.add(community);
		}
		for(int i=0; i<memberships.rows(); i++) {
			NonZeroEntriesVectorProcedure procedure = new NonZeroEntriesVectorProcedure();
			memberships.getRow(i).eachNonZero(procedure);
			List<Integer> nonZeroEntries = procedure.getNonZeroEntries();
			for(int j : nonZeroEntries) {
				Community community = communities.get(j);
				community.setBelongingFactor(graph.getCustomNode(nodes[i]), memberships.get(i, j));
			}
			
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Community> getCommunities() {
		return communities;
	}

	public void setCommunities(List<Community> communities) {
		if(communities != null) {
			this.communities = communities;
		}
		else {
			this.communities = new ArrayList<Community>();
		}
	}

	public AlgorithmLog getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(AlgorithmLog algorithm) {
		if(algorithm != null) {
			this.algorithm = algorithm;
		}
		else {
			this.algorithm = new AlgorithmLog(AlgorithmIdentifier.UNDEFINED, new HashMap<String, String>());
		}
	}

	public List<MetricLog> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<MetricLog> metrics) {
		this.metrics.clear();
		for(MetricLog metric : metrics) {
			if(metric != null)
				this.metrics.add(metric);
		}
	}

	/**
	 * Returns the first metric occurrence with the corresponding identifier.
	 * @param metricId
	 * @return The metric. Null if no such metric exists.
	 */
	public MetricLog getMetric(MetricIdentifier metricId) {
		for(MetricLog metric : this.metrics){
			if(metricId == metric.getIdentifier()) {
				return metric;
			}
		}
		return null;
	}
	
	public void setMetric(MetricLog metric) {
		if(metric != null) {
			this.metrics.add(metric);
		}
	}
	
	public int communityCount() {
		return communities.size();
	}
	
	public List<Integer> getCommunityIndices(Node node) {
		List<Integer> communityIndices = new ArrayList<Integer>();
		for(int j=0; j < communities.size(); j++) {
			if(this.communities.get(j).getBelongingFactor(graph.getCustomNode(node)) > 0) {
				communityIndices.add(j);
			}
		}
		return communityIndices;
	}
	
	public double getBelongingFactor(Node node, int communityIndex) {
		return communities.get(communityIndex).getBelongingFactor(graph.getCustomNode(node));
	}
	
	public void setBelongingFactor(Node node, int communityIndex, double belongingFactor) {
		communities.get(communityIndex).setBelongingFactor(graph.getCustomNode(node), belongingFactor);
	}
	
	public String getCommunityName(int communityIndex) {
		return communities.get(communityIndex).getName();
	}
	
	public void setCommunityName(int communityIndex, String name) {
		communities.get(communityIndex).setName(name);
	}
	
	public Color getCommunityColor(int communityIndex) {
		return communities.get(communityIndex).getColor();
	}
	
	public void setCommunityColor(int communityIndex, Color color) {
		communities.get(communityIndex).setColor(color);
	}
	
	/**
	 * Normalizes each row of the membership matrix using the one norm.
	 * All matrix results are removed.
	 * Note that a row stays equal to 0 if all values are 0 already.
	 */
	public void normalizeMemberships() {
		normalizeMemberships(this.getMemberships());
	}
	
	/*
	 * Overload for internal reuse and performance.
	 * Normalizes each row of the membership matrix using the one norm.
	 * All matrix results are removed.
	 * Note that a row stays equal to 0 if all values are 0 already.
	 * @param memberships The memberships matrix to be normalized and set.
	 */
	protected void normalizeMemberships(Matrix memberships) {
		for(int i=0; i<memberships.rows(); i++) {
			Vector row = memberships.getRow(i);
			double norm = row.fold(Vectors.mkManhattanNormAccumulator());
			if(norm != 0) {
				row = row.divide(norm);
				memberships.setRow(i, row);
			}
		}
		this.setMemberships(memberships);
		metrics.clear();
	}
	
	/**
	 * Filters the cover membership matrix by removing insignificant membership values.
	 * The cover is then normalized and empty communities are removed. All metric results
	 * are removed as well.
	 * All entries below the threshold will be set to 0, unless they are the maximum 
	 * belonging factor of the node.
	 * @param threshold 
	 * 
	 */
	public void filterMembershipsbyThreshold(double threshold) {
		Matrix memberships = this.getMemberships();
		for(int i=0; i<memberships.rows(); i++) {
			setRowEntriesBelowThresholdToZero(memberships, i, threshold);
		}
		normalizeMemberships(memberships);
		this.setMemberships(memberships);
		removeEmptyCommunities();
		metrics.clear();
	}

	@Override
	public String toString() {
		String coverString = "Cover: " + getName() + "\n";
		coverString += "Graph: " + getGraph().getName() + "\n";
		coverString += "Algorithm: " + getAlgorithm().getIdentifier().toString() + "\n" + "params:" + "\n";
		for(Map.Entry<String, String> entry : getAlgorithm().getParameters().entrySet()) {
			coverString += entry.getKey() + " = " + entry.getValue() + "\n";
		}
		coverString += "Community Count: " + communityCount() + "\n";
		MetricLog metric;
		for(int i=0; i<metrics.size(); i++) {
			metric = metrics.get(i);
			coverString += metric.getIdentifier().toString() + " = ";
			coverString += metric.getValue() + "\n" + "params:" + "\n";
			for(Map.Entry<String, String> entry : metric.getParameters().entrySet()) {
				coverString += entry.getKey() + " = " + entry.getValue() + "\n";
			}
			coverString += "\n";
		}
		coverString += "Membership Matrix\n";
		coverString += getMemberships().toString();
		return coverString;
	}
	
	public int getCommunitySize(int communityIndex) {
		return communities.get(communityIndex).getSize();
	}
	
	/*
	 * Sets all matrix entries of one row which are lower than the threshold and the rows max entry to zero.
	 * @param matrix The matrix being filtered.
	 * @param rowIndex The index of the row being filtered.
	 * @param threshold The threshold.
	 */
	protected void setRowEntriesBelowThresholdToZero(Matrix matrix, int rowIndex, double threshold) {
		Vector row = matrix.getRow(rowIndex);
		double rowThreshold = Math.min(row.fold(Vectors.mkMaxAccumulator()), threshold);
		BelowThresholdEntriesVectorProcedure procedure = new BelowThresholdEntriesVectorProcedure(rowThreshold);
		row.eachNonZero(procedure);
		List<Integer> belowThresholdEntries = procedure.getBelowThresholdEntries();
		for(int i : belowThresholdEntries) {
			row.set(i, 0);
		}
		matrix.setRow(rowIndex, row);
	}
	
	/*
	 * Removes all empty communities from the graph.
	 * A community is considered as empty when the corresponding belonging factor
	 * equals 0 for each node.
	 */
	protected void removeEmptyCommunities() {
		Iterator<Community> it = communities.iterator();
		while(it.hasNext()) {
			Community community = it.next();
			if(community.getSize() == 0) {
				it.remove();
			}
		}
	}
	
}
