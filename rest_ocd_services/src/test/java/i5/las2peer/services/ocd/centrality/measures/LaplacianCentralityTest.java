package i5.las2peer.services.ocd.centrality.measures;

import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmException;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithmExecutor;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

public class LaplacianCentralityTest {
	@Test
	public void testUndirectedUnweighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedUnweighted();
		LaplacianCentrality algorithm = new LaplacianCentrality();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Laplacian Centrality (Undirected, Unweighted)");
		System.out.println(result.toString());
	}
	
	@Test
	public void testUndirectedWeighted() throws InterruptedException, CentralityAlgorithmException {
		CustomGraph graph = OcdTestGraphFactory.getSimpleGraphUndirectedWeighted();
		LaplacianCentrality algorithm = new LaplacianCentrality();
		CentralityAlgorithmExecutor executor = new CentralityAlgorithmExecutor();
		CentralityMap result = executor.execute(graph, algorithm);
		result.setName("Laplacian Centrality (Undirected, Weighted)");
		System.out.println(result.toString());
	}
}
