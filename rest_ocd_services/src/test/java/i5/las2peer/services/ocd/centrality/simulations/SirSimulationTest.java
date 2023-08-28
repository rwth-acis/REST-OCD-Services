package i5.las2peer.services.ocd.centrality.simulations;

import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmException;
import i5.las2peer.services.ocd.centrality.utils.CentralitySimulationExecutor;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class SirSimulationTest {
	@Test
	public void testUndirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedUnweighted();
		SirSimulation algorithm = new SirSimulation();
		CentralitySimulationExecutor executor = new CentralitySimulationExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("SIR (Undirected, Unweighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testDirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphDirectedUnweighted();
		SirSimulation algorithm = new SirSimulation();
		CentralitySimulationExecutor executor = new CentralitySimulationExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("SIR (Directed, Unweighted)");
		System.out.println(result.toString());
	}
}
