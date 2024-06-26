package i5.las2peer.services.ocd.centrality.measures;

import org.junit.Test;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmException;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmExecutor;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class IntegrationTest {
	@Test
	public void testUndirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedUnweighted();

		Integration algorithm = new Integration();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Integration (Undirected, Unweighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testUndirectedWeighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedWeighted();
		Integration algorithm = new Integration();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Integration (Undirected, Weighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testDirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphDirectedUnweighted();
		Integration algorithm = new Integration();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Integration (Directed, Unweighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testDirectedWeighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphDirectedWeighted();
		Integration algorithm = new Integration();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Integration (Directed, Weighted)");
		System.out.println(result.toString());
	}
}
