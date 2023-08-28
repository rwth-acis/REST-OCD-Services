package i5.las2peer.services.ocd.centrality.simulations;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmException;
import i5.las2peer.services.ocd.centrality.utils.CentralitySimulationExecutor;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.jupiter.api.Test;

public class RandomPackageTransmissionWeightedTest {
	@Test
	public void testUndirectedWeighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedWeighted();
		RandomPackageTransmissionWeighted algorithm = new RandomPackageTransmissionWeighted();
		CentralitySimulationExecutor executor = new CentralitySimulationExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Random Package Transmission Weighted (Undirected, Weighted)");
		System.out.println(result.toString());
	}
}
