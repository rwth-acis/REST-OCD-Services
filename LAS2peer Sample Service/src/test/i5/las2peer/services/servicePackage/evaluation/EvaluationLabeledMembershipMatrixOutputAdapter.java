package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.AdapterException;
import i5.las2peer.services.servicePackage.adapters.coverOutput.AbstractCoverOutputAdapter;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.metrics.MetricLog;

import java.io.Writer;

import y.base.Node;
import y.base.NodeCursor;

public class EvaluationLabeledMembershipMatrixOutputAdapter extends AbstractCoverOutputAdapter {

	public EvaluationLabeledMembershipMatrixOutputAdapter(Writer writer) {
		this.setWriter(writer);
	}
	
	@Override
	public void writeCover(Cover cover) throws AdapterException {
		try {
			for(MetricLog metric : cover.getMetrics()) {
				writer.write(metric.getType().name() + ": " + metric.getValue() + "\n");
			}
			CustomGraph graph = cover.getGraph();
			NodeCursor nodes = graph.nodes();
			while(nodes.ok()) {
				Node node = nodes.node();
				String nodeName = graph.getNodeName(node);
				if(nodeName.isEmpty()) {
					nodeName = Integer.toString(node.index());
				}
				writer.write(nodeName + " ");
				for(int i=0; i<cover.communityCount(); i++) {
					writer.write(String.format("%.4f ", cover.getBelongingFactor(node, i)));
				}
				writer.write("\n");
				nodes.next();
			}
		}
		catch (Exception e) {
			throw new AdapterException(e);
		}
		finally {
			try {
				writer.close();
			}
			catch (Exception e) {
			}
		}
	}

	
	
}
