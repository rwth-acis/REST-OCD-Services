package i5.las2peer.services.ocd.metrics;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.GraphType;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

/**
 * Implements the multiplex modularity metric.
 */
public class MultiplexModularityMetric implements StatisticalMeasure {

	public MultiplexModularityMetric() {
	}

	@Override
	public void setParameters(Map<String, String> parameters) {
	}

	@Override
	public Map<String, String> getParameters() {
		return new HashMap<String, String>();
	}

	@Override
	public double measure(Cover cover) throws OcdMetricException, InterruptedException, OcdAlgorithmException {
		CustomGraph graph = cover.getGraph();
		Map<String, Matrix> networks = createAdjacencyMatrix(graph);
		Map<String, Integer> layerEdgeCount = layerEdgeCount(networks);

		double overallModularity = 0;
		double edgeCount = 0;

		for (Map.Entry<String, Matrix> entry : networks.entrySet()) {

			edgeCount = edgeCount + (layerEdgeCount.get(entry.getKey()) / 2);
			double modularity = 0;

			for (int i = 0; i < graph.getNodeCount(); i++) {

				double deg1 = numberOfNeighbors(entry.getValue(), i);
				Node node1 = graph.getNode(i);
				List<Integer> com1 = cover.getMultiplexCommunityIndices(entry.getKey(), node1);

				for (int j = i + 1; j < graph.getNodeCount(); j++) {
					Node n2 = graph.getNode(j);
					List<Integer> com2 = cover.getMultiplexCommunityIndices(entry.getKey(), n2);
					com2.retainAll(com1);
					if (com2.size() != 0) {
						double deg2 = numberOfNeighbors(entry.getValue(), j);
						modularity -= deg1 * deg2 / (2 * edgeCount);
						if (entry.getValue().get(i, j) > 0) {
							modularity += 1;
						}
					}

				}

			}
			overallModularity = overallModularity + modularity;

		}

		return overallModularity / edgeCount;
	}

	/**
	 * Returns the graph types which are compatible for a metric.
	 * 
	 * @return The compatible graph types.
	 */
	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.MULTIPLE_EDGES);
		return compatibleTypes;
	}

	/**
	 * This method creates a Mapping from layerID to adjacency matrix.
	 * 
	 * @param graph Graph based on which the adjacency matrix should be built
	 * @return Adjacency matrix based on the input graph
	 */
	public Map<String, Matrix> createAdjacencyMatrix(CustomGraph graph) {

		Iterator<Edge> edges = graph.edges().iterator();
		Map<String, Matrix> matrices = new HashMap<>();

		while (edges.hasNext()) {
			Edge edge = edges.next();
			if (!matrices.containsKey(graph.getEdgeLayerId(edge))) {
				Matrix A = new Basic2DMatrix(graph.getNodeCount(), graph.getNodeCount());
				A = A.blank();
				matrices.put(graph.getEdgeLayerId(edge), A);
			}
			matrices.get(graph.getEdgeLayerId(edge)).set(edge.getSourceNode().getIndex(),
					edge.getTargetNode().getIndex(), 1);
		}

		return matrices;

	}

	public Map<String, Integer> layerEdgeCount(Map<String, Matrix> networks) {

		Map<String, Integer> layerEdgeCount = new HashMap<>();

		for (Map.Entry<String, Matrix> entry : networks.entrySet()) {

			int egdeCount = 0;

			for (int i = 0; i < entry.getValue().columns(); i++) {
				for (int j = 0; j < entry.getValue().columns(); j++) {
					if (entry.getValue().get(i, j) == 1) {
						egdeCount++;
					}
				}
			}
			layerEdgeCount.put(entry.getKey(), egdeCount);

		}
		return layerEdgeCount;
	}

	public int numberOfNeighbors(Matrix matrix, int nodeId) {

		Vector vector = matrix.getRow(nodeId);
		int number = 0;
		for (int i = 0; i < vector.length(); i++) {
			if (vector.get(i) == 1) {
				number++;
			}
		}
		return number;

	}

}
