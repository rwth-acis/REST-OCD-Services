package i5.las2peer.services.ocd.centrality.measures;

import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmException;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmExecutor;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class PageRankTest {
	@Test
	public void testUndirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedUnweighted();
		PageRank algorithm = new PageRank();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("PageRank (Undirected, Unweighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testUndirectedWeighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedWeighted();
		PageRank algorithm = new PageRank();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("PageRank (Undirected, Weighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testDirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphDirectedUnweighted();
		PageRank algorithm = new PageRank();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("PageRank (Directed, Unweighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testDirectedWeighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphDirectedWeighted();
		PageRank algorithm = new PageRank();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("PageRank (Directed, Weighted)");
		System.out.println(result.toString());
	}
}
