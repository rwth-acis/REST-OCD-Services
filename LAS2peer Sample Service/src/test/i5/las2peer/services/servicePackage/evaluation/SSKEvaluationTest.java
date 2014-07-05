package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverOutput.LabeledMembershipMatrixOutputAdapter;
import i5.las2peer.services.servicePackage.algorithms.AlgorithmFactory;
import i5.las2peer.services.servicePackage.algorithms.OverlappingCommunityDetectionAlgorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.metrics.ExtendedModularityMetric;
import i5.las2peer.services.servicePackage.metrics.StatisticalMeasure;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.io.IOException;

import org.junit.Test;

/*
 * Test Class for the Speaker Listener Label Propagation Algorithm
 */
public class SSKEvaluationTest {

	/**
	 * Test the SLPA Algorithm on a simple Graph
	 * @throws IOException 
	 */

	@Test
	public void testSskAlgoOnSiamDm() throws IOException
	{
		CustomGraph graph = OcdTestGraphFactory.getSiamDmGraph();
		AlgorithmFactory factory = AlgorithmFactory.getAlgorithmFactory();
		OverlappingCommunityDetectionAlgorithm algo = 
				factory.getStandardSSKAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		System.out.println("Memberships");
		System.out.println(cover.getMemberships());
		System.out.println();
		StatisticalMeasure extendedModularity = new ExtendedModularityMetric();
		double modularity = extendedModularity.getValue(cover);
		System.out.println("Modularity:");
		System.out.println(modularity);
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter();
		adapter.setFilename(OcdTestConstants.sskSiamDmLabeledMembershipMatrixOutputPath);
		adapter.writeCover(cover);
	}
}
