package i5.las2peer.services.ocd.centrality.simulations;

import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmException;
import i5.las2peer.services.ocd.centrality.utils.CentralitySimulationExecutor;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class RandomPackageTransmissionUnweightedTest {
	@Test
	public void testUndirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedUnweighted();
		RandomPackageTransmissionUnweighted algorithm = new RandomPackageTransmissionUnweighted();
		CentralitySimulationExecutor executor = new CentralitySimulationExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Random Package Transmission Unweighted (Undirected, Unweighted)");
		System.out.println(result.toString());
	}
}
