package i5.las2peer.services.ocd.metrics;

import java.io.FileNotFoundException;
import java.text.ParseException;

import org.junit.jupiter.api.Test;
import org.la4j.matrix.Matrix;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;
import org.la4j.matrix.dense.Basic2DMatrix;

public class ModularityMetricTest {
	

	@Test
	public void testModularity() throws OcdAlgorithmException, InterruptedException, AdapterException, FileNotFoundException, IllegalArgumentException, ParseException, OcdMetricException {
		CustomGraph graph = OcdTestGraphFactory.getMaximalCliqueGraph();
		ModularityMetric MM = new ModularityMetric();
		
		Cover cover = new Cover(graph);
		Matrix memberships = new Basic2DMatrix(9,2); 
		memberships.set(0,0,1); 
		memberships.set(1,0,1); 
		memberships.set(2,1,1); 
		memberships.set(3,1,1); 
		memberships.set(4,1,1); 
		memberships.set(5,1,1); 
		memberships.set(6,1,1); 
		memberships.set(7,1,1); 
		memberships.set(8,1,1); 
		
		cover.setMemberships(memberships);  
		System.out.println(cover);
		double modularity = MM.measure(cover); 
		System.out.println(modularity);
		
		CustomGraph graph2 = OcdTestGraphFactory.getModularityTestGraph();
		
		
		System.out.println(graph2.getNodeCount());
		Cover cover2 = new Cover(graph2);
		Matrix memberships2 = new Basic2DMatrix(4,2); 
		memberships2.set(0,0,1); 
		memberships2.set(1,0,1); 
		memberships2.set(2,0,1); 
		memberships2.set(3,1,1); 

		
		cover2.setMemberships(memberships2);  
		System.out.println(cover2);
		modularity = MM.measure(cover2); 
		System.out.println(modularity);

		
	}

}