package i5.las2peer.services.ocd.evaluation;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.adapters.coverOutput.AbstractCoverOutputAdapter;
import i5.las2peer.services.ocd.graph.Cover;
import i5.las2peer.services.ocd.graph.CustomGraph;
import i5.las2peer.services.ocd.metrics.MetricLog;

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
