package i5.las2peer.services.ocd.centrality.measures;

import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmException;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmExecutor;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class LeaderRankTest {
	@Test
	public void testUndirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedUnweighted();
		LeaderRank algorithm = new LeaderRank();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("LeaderRank (Undirected, Unweighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testUndirectedWeighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedWeighted();
		LeaderRank algorithm = new LeaderRank();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("LeaderRank (Undirected, Weighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testDirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphDirectedUnweighted();
		LeaderRank algorithm = new LeaderRank();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("LeaderRank (Directed, Unweighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testDirectedWeighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphDirectedWeighted();
		LeaderRank algorithm = new LeaderRank();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("LeaderRank (Directed, Weighted)");
		System.out.println(result.toString());
	}
}
