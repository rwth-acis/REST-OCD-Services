package i5.las2peer.services.ocd.algorithms.ClusteringUtils;

import java.io.FileNotFoundException;
import java.text.ParseException;

import org.junit.jupiter.api.Disabled;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.algorithms.utils.Termmatrix;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.junit.jupiter.api.Test;

public class TermmatrixTest {
	

	@Test
	public void testOnJmolTestGraph() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException {
		CustomGraph graph = OcdTestGraphFactory.getJmolTestGraph();
		Termmatrix tm = new Termmatrix(graph);
		////System.out.println(tm.toString(graph));
	}
}
