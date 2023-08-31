package i5.las2peer.services.ocd.graphs.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import i5.las2peer.services.ocd.graphs.CustomGraph;


@ExtendWith(MockitoExtension.class)
public class AverageDegreeTest {
	
	@Spy
	AverageDegree property;
	
	@Mock
	CustomGraph graph;
	
	@Test
	public void calculate() {

		AverageDegree property = new AverageDegree();
		double result;

		result = property.calculate(5, 4);
		assertEquals(1.6, result, 0.00001);

		result = property.calculate(1, 4);
		assertEquals(8.0, result, 0.00001);

		result = property.calculate(6, 10);
		assertEquals(3.33333, result, 0.00001);
	}
	
	@Test
	public void calculateInvalid() {

		AverageDegree property = new AverageDegree();
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
				
		Mockito.when(graph.getNodeCount()).thenReturn(4);
		Mockito.when(graph.getEdgeCount()).thenReturn(8);
				
		property.calculate(graph);
		Mockito.verify(property, Mockito.times(1)).calculate(4, 8);
	}
	
}
