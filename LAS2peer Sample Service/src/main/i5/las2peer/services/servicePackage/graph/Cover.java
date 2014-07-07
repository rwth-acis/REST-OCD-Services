package i5.las2peer.services.servicePackage.graph;

import i5.las2peer.services.servicePackage.algorithms.Algorithm;
import i5.las2peer.services.servicePackage.metrics.Metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;

import y.base.Node;

public class Cover {

	private CustomGraph graph;
	private Matrix memberships;
	private String name;
	private Algorithm algorithm;
	private Map<Metric, Double> metricResults;
	
	public Cover(CustomGraph graph, Matrix memberships, Algorithm algorithm) {
		setGraph(graph);
		setMemberships(memberships);
		setAlgorithm(algorithm);
		metricResults = new HashMap<Metric, Double>();
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
	
	public List<Integer> getCommunityIndices(int nodeIndex) {
		List<Integer> communities = new ArrayList<Integer>();
		for(int j=0; j < memberships.columns(); j++) {
			if(memberships.get(nodeIndex, j) > 0) {
				communities.add(j);
			}
		}
		return communities;
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
	 * A row stays equal if the sum of the absolute values of all entries equals 0.
	 */
	public void doNormalize() {
		for(int i=0; i<memberships.rows(); i++) {
			Vector row = memberships.getRow(i);
			double norm = row.fold(Vectors.mkManhattanNormAccumulator());
			if(norm != 0) {
				row = row.divide(norm);
				memberships.setRow(i, row);
			}
		}
	}

	@Override
	public String toString() {
		String coverString = "Cover:" + getName() + "\n";
		coverString += "Graph: " + getGraph().getName() + "\n";
		coverString += "Algorithm: " + getAlgorithm().toString() + "\n";
		coverString += "Membership Matrix\n";
		Metric metric;
		for(int i=0; i<Metric.values().length; i++) {
			metric = Metric.values()[i];
			if(metricResults.containsKey(metric)) {
				coverString += metric.toString();
				coverString += metricResults.get(metric);
				coverString += "\n";
			}
		}
		coverString += getMemberships().toString();
		return coverString;
	}
	
}
