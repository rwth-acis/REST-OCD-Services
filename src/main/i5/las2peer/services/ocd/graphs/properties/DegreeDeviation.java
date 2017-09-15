package i5.las2peer.services.ocd.graphs.properties;

import org.apache.commons.math3.stat.StatUtils;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.base.Node;
import y.base.NodeCursor;

/**
 * This class handles the degree deviation computation of a CustomGraph.
 */
public class DegreeDeviation extends GraphPropertyAbstract {
	
	/**
	 * Returns the degree deviation of a CustomGraph
	 * 
	 * @param graph the CustomGraph
	 * @return the degree deviation
	 */
	@Override
	public double calculate(CustomGraph graph) {

		if (graph == null)
			throw new IllegalArgumentException();

		double[] degrees = new double[graph.nodeCount()];
		int nodeId = 0;
		for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
			Node node = nc.node();			
			if(graph.isDirected()) {
				degrees[nodeId] = node.degree();
			} else {
				degrees[nodeId] = node.degree() / 2;
			}
			nodeId++;
		}
		return calculate(degrees);

	}

	/**
	 * @param values degree values of nodes
	 * @return degree deviation
	 */
	protected double calculate(double[] values) {

		if (values == null)
			return 0.0;

		double deviation = Math.sqrt(StatUtils.variance(values));
		if (Double.isNaN(deviation))
			return 0.0;

		return deviation;
	}

}
