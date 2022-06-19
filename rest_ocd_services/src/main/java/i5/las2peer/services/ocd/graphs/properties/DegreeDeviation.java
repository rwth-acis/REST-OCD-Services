package i5.las2peer.services.ocd.graphs.properties;

import org.apache.commons.math3.stat.StatUtils;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.graphstream.graph.Node;

import java.util.Iterator;

/**
 * This class handles the degree deviation computation of a CustomGraph.
 */
public class DegreeDeviation extends AbstractProperty {
	
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

		double[] degrees = new double[graph.getNodeCount()];
		int nodeId = 0;
		Iterator<Node> nc = graph.iterator();
		while (nc.hasNext()) {
			Node node = nc.next();
			if(graph.isDirected()) {
				degrees[nodeId] = node.getDegree();
			} else {
				degrees[nodeId] = node.getDegree() / 2;
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
