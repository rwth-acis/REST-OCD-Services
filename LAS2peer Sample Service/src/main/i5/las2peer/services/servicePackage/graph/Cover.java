package i5.las2peer.services.servicePackage.graph;

import i5.las2peer.services.servicePackage.algorithms.Algorithm;
import i5.las2peer.services.servicePackage.metrics.Metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;

import y.base.Node;

public class Cover {

	private CustomGraph graph = new CustomGraph();
	private Matrix memberships = new CCSMatrix();
	private String name = "";
	private Algorithm algorithm = Algorithm.UNDEFINED;
	private Map<Metric, Double> metricResults = new HashMap<Metric, Double>();
	
	public Cover(CustomGraph graph, Matrix memberships, Algorithm algorithm) {
		setGraph(graph);
		setMemberships(memberships);
		setAlgorithm(algorithm);
	}

	public CustomGraph getGraph() {
		return graph;
	}

	public void setGraph(CustomGraph graph) {
		this.graph = graph;
	}

	public Matrix getMemberships() {
		return memberships;
	}

	public void setMemberships(Matrix memberships) {
		this.memberships = memberships;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(Algorithm algorithm) {
		if(algorithm != null) {
			this.algorithm = algorithm;
		}
		else {
			this.algorithm = Algorithm.UNDEFINED;
		}
	}

	public Map<Metric, Double> getMetricResults() {
		return metricResults;
	}

	public void setMetricResults(Map<Metric, Double> metricResults) {
		this.metricResults.clear();
		for(Map.Entry<Metric, Double> entry : metricResults.entrySet()) {
			if(entry.getKey() != null && entry.getValue() != null)
				this.metricResults.put(entry.getKey(), entry.getValue());
		}
	}

	public double getMetricResult(Metric metric) {
		if(this.metricResults.containsKey(metric)) {
			return metricResults.get(metric);
		}
		else {
			return Double.NaN;
		}
	}
	
	public void setMetricResult(Metric metric, double result) {
		if(metric != null) {
			this.metricResults.put(metric, result);
		}
	}
	
	public int communityCount() {
		return memberships.columns();
	}
	
	public List<Integer> getCommunityIndices(Node node) {
		List<Integer> communities = new ArrayList<Integer>();
		for(int j=0; j < memberships.columns(); j++) {
			if(memberships.get(node.index(), j) > 0) {
				communities.add(j);
			}
		}
		return communities;
	}
	
	public double getBelongingFactor(Node node, int communityIndex) {
		return memberships.get(node.index(), communityIndex);
	}
	
	/**
	 * Normalizes each row of the membership matrix using the one norm.
	 * All matrix results are removed.
	 * Note that a row stays equal to 0 if all values are 0 already.
	 */
	public void normalizeMemberships() {
		for(int i=0; i<memberships.rows(); i++) {
			Vector row = memberships.getRow(i);
			double norm = row.fold(Vectors.mkManhattanNormAccumulator());
			if(norm != 0) {
				row = row.divide(norm);
				memberships.setRow(i, row);
			}
		}
		setMetricResults(new HashMap<Metric, Double>());
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
		for(int i=0; i<memberships.rows(); i++) {
			setRowEntriesBelowThresholdToZero(i, threshold);
		}
		normalizeMemberships();
		removeEmptyCommunities();
		setMetricResults(new HashMap<Metric, Double>());
	}
	
	/**
	 * Removes all empty communities from the graph.
	 * A community is considered as empty when the corresponding belonging factor
	 * equals 0 for each node.
	 */
	public void removeEmptyCommunities() {
		Vector column;
		List<Integer> nonZeroIndices = new ArrayList<Integer>();
		for(int i=0; i<memberships.columns(); i++) {
			column = memberships.getColumn(i);
			if(!column.is(Vectors.ZERO_VECTOR)) {
				nonZeroIndices.add(i);
			}
		}
		int[] rowIndices = new int[graph.nodeCount()];
		for(int i=0; i<rowIndices.length; i++) {
			rowIndices[i] = i;
		}
		int[] columnIndices = new int[nonZeroIndices.size()];
		for(int i=0; i<columnIndices.length; i++) {
			columnIndices[i] = nonZeroIndices.get(i);
		}
		memberships = memberships.select(rowIndices, columnIndices);
	}

	@Override
	public String toString() {
		String coverString = "Cover:" + getName() + "\n";
		coverString += "Graph: " + getGraph().getName() + "\n";
		coverString += "Algorithm: " + getAlgorithm().toString() + "\n";
		coverString += "Community Count: " + communityCount() + "\n";
		Metric metric;
		for(int i=0; i<Metric.values().length; i++) {
			metric = Metric.values()[i];
			if(metricResults.containsKey(metric)) {
				coverString += metric.toString();
				coverString += metricResults.get(metric);
				coverString += "\n";
			}
		}
		coverString += "Membership Matrix\n";
		coverString += getMemberships().toString();
		return coverString;
	}
	
	/*
	 * Sets all entries of a row which are lower than the threshold and the rows max entry to zero.
	 * @param rowIndex The index of the row to be filtered.
	 * @param threshold The threshold.
	 */
	protected void setRowEntriesBelowThresholdToZero(int rowIndex, double threshold) {
		Vector row = memberships.getRow(rowIndex);
		double rowThreshold = Math.min(row.fold(Vectors.mkMaxAccumulator()), threshold);
		BelowThresholdEntriesVectorProcedure procedure = new BelowThresholdEntriesVectorProcedure(rowThreshold);
		row.eachNonZero(procedure);
		List<Integer> belowThresholdEntries = procedure.getBelowThresholdEntries();
		for(int i : belowThresholdEntries) {
			row.set(i, 0);
		}
		memberships.setRow(rowIndex, row);
	}
	
}
