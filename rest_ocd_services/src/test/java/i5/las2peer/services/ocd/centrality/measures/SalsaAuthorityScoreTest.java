package i5.las2peer.services.ocd.centrality.measures;

import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmException;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmExecutor;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class SalsaAuthorityScoreTest {
	@Test
	public void testUndirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedUnweighted();
		SalsaAuthorityScore algorithm = new SalsaAuthorityScore();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Salsa Authority Score (Undirected, Unweighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testUndirectedWeighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedWeighted();
		SalsaAuthorityScore algorithm = new SalsaAuthorityScore();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Salsa Authority Score (Undirected, Weighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testDirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphDirectedUnweighted();
		SalsaAuthorityScore algorithm = new SalsaAuthorityScore();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Salsa Authority Score (Directed, Unweighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testDirectedWeighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphDirectedWeighted();
		SalsaAuthorityScore algorithm = new SalsaAuthorityScore();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Salsa Authority Score (Directed, Weighted)");
		System.out.println(result.toString());
	}
}
