package i5.las2peer.services.ocd.graphs.properties;

import org.apache.commons.math3.stat.StatUtils;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import y.base.Node;
import y.base.NodeCursor;

public class DegreeDeviation extends CustomGraphProperty {

	@Override
	public double calculate(CustomGraph graph) {

		if (graph == null)
			throw new IllegalArgumentException();

		double[] degrees = new double[graph.nodeCount()];
		int nodeId = 0;
		for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
			Node node = nc.node();
			degrees[nodeId] = node.degree();
			nodeId++;
		}
		return calculate(degrees);

	}

	protected double calculate(double[] values) {

		if (values == null)
			return 0.0;

		double deviation = Math.sqrt(StatUtils.variance(values));
		if (Double.isNaN(deviation))
			return 0.0;

		return deviation;
	}

}
