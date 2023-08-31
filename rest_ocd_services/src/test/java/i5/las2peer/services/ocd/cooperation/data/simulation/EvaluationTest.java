package i5.las2peer.services.ocd.cooperation.data.simulation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class EvaluationTest {
	
	double[] values;
	double result;
	Evaluation evaluation;
	
	@Test
	public void averageTest() {
		
		values = new double[]{2.4, 4.2, 3.3, 2.1};
		evaluation = new Evaluation(values);		
		result = evaluation.getAverage();
		assertEquals(3.0, result, 0.01);
		
		values = new double[]{800.0, 1200.0, 400.0};
		evaluation = new Evaluation(values);		
		result = evaluation.getAverage();
		assertEquals(800, result, 0.01);
		
		values = new double[]{0.0, 0.0, 0.0};
		evaluation = new Evaluation(values);		
		result = evaluation.getAverage();
		assertEquals(0, result, 0.01);		

	}
	
	@Test
	public void varianceTest() {
		
		values = new double[]{2.4, 4.2, 3.3, 2.1};
		evaluation = new Evaluation(values);		
		result = evaluation.getVariance();
		assertEquals(0.9, result, 0.01);
		
		values = new double[]{800.0, 1200.0, 400.0};
		evaluation = new Evaluation(values);		
		result = evaluation.getVariance();
		assertEquals(160000, result, 0.01);
		
		values = new double[]{0.0, 0.0, 0.0};
		evaluation = new Evaluation(values);		
		result = evaluation.getVariance();
		assertEquals(0, result, 0.01);

	}
	
	@Test
	public void deviationTest() {
		
		values = new double[]{1.3, 4.2, 3.9, 2.1};
		evaluation = new Evaluation(values);		
		result = evaluation.getDeviation();
		assertEquals(1.4, result, 0.01);
		
		values = new double[]{800.0, 1200.0, 400.0};
		evaluation = new Evaluation(values);		
		result = evaluation.getDeviation();
		assertEquals(400, result, 0.01);
		
		values = new double[]{0.0, 0.0, 0.0};
		evaluation = new Evaluation(values);		
		result = evaluation.getDeviation();
		assertEquals(0, result, 0.01);

	}
	
	@Test
	public void minTest() {
		
		values = new double[]{1.4, 4.8, 0.7, 2.2};
		evaluation = new Evaluation(values);		
		result = evaluation.getMinimum();
		assertEquals(0.7, result, 0.0001);
		
		values = new double[]{800.0, 1200.0, 400.0};
		evaluation = new Evaluation(values);		
		result = evaluation.getMinimum();
		assertEquals(400, result, 0.0001);
		
		values = new double[]{0.0, 0.0, 0.0};
		evaluation = new Evaluation(values);		
		result = evaluation.getMinimum();
		assertEquals(0, result, 0.0001);
	}
	
	@Test
	public void maxTest() {
		
		values = new double[]{1.4, 4.8, 0.7, 2.2};
		evaluation = new Evaluation(values);		
		result = evaluation.getMaximum();
		assertEquals(4.8, result, 0.0001);
		
		values = new double[]{800.0, 1200.0, 400.0};
		evaluation = new Evaluation(values);		
		result = evaluation.getMaximum();
		assertEquals(1200, result, 0.0001);
		
		values = new double[]{0.0, 0.0, 0.0};
		evaluation = new Evaluation(values);		
		result = evaluation.getMaximum();
		assertEquals(0, result, 0.0001);

	}
	
	@Test
	public void averageEmpty() {
		
		values = new double[]{};
		evaluation = new Evaluation(values);		
		result = evaluation.getAverage();
		assertEquals(0.0, result, 0.01);
	}
	
	@Test
	public void varianceEmpty() {
		
		values = new double[]{};
		evaluation = new Evaluation(values);		
		result = evaluation.getVariance();
		assertEquals(0.0, result, 0.01);
	}
	
	@Test
	public void deviationEmpty() {
		
		values = new double[]{};
		evaluation = new Evaluation(values);		
		result = evaluation.getDeviation();
		assertEquals(0.0, result, 0.01);
	}
	
	@Test
	public void averageNull() {
		
		values = null;
		evaluation = new Evaluation(values);		
		result = evaluation.getAverage();
		assertEquals(0.0, result, 0.01);
	}
	
	@Test
	public void varianceNull() {
		
		values = null;
		evaluation = new Evaluation(values);		
		result = evaluation.getVariance();
		assertEquals(0.0, result, 0.01);
	}
	
	@Test
	public void deviationNull() {
		
		values = null;
		evaluation = new Evaluation(values);		
		result = evaluation.getDeviation();
		assertEquals(0.0, result, 0.01);
	}
	
	
}
