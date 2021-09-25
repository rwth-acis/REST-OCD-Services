package i5.las2peer.services.ocd.graphs.properties;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import i5.las2peer.services.ocd.graphs.CustomGraph;

@RunWith(MockitoJUnitRunner.class)
public class DensityTest {
	
	@Spy
	Density property;
	
	@Mock
	CustomGraph graph;
	
	@Test
	public void calculate() {

		Density density = new Density();
		double result;

		result = density.calculate(5, 4);
		assertEquals(0.2, result, 0.00001);

		result = density.calculate(1, 4);
		assertEquals(0.0, result, 0.00001);

		result = density.calculate(6, 10);
		assertEquals(0.33333, result, 0.00001);
	}
	
	@Test
	public void calculateInvalid() {

		Density property = new Density();
		double result;

		result = property.calculate(-2, -5);
		assertEquals(0.0, result, 0.00001);

		result = property.calculate(2, -4);
		assertEquals(0.0, result, 0.00001);
		
		result = property.calculate(-3, 5);
		assertEquals(0.0, result, 0.00001);

	}
	
	@Test
	public void initialize() {
				
		Mockito.when(graph.nodeCount()).thenReturn(4);
		Mockito.when(graph.edgeCount()).thenReturn(7);
				
		property.calculate(graph);
		Mockito.verify(property, Mockito.times(1)).calculate(4, 7);
	}
	
}
