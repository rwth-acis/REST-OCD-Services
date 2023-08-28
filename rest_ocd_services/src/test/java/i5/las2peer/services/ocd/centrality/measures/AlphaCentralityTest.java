package i5.las2peer.services.ocd.centrality.measures;

import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmException;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmExecutor;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class AlphaCentralityTest {
	@Test
	public void testUndirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedUnweighted();
		AlphaCentrality algorithm = new AlphaCentrality();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Alpha Centrality (Undirected, Unweighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testDirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphDirectedUnweighted();
		AlphaCentrality algorithm = new AlphaCentrality();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Alpha Centrality (Directed, Unweighted)");
		System.out.println(result.toString());
	}
}
