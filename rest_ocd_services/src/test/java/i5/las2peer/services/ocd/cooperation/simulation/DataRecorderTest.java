package i5.las2peer.services.ocd.cooperation.simulation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class DataRecorderTest {

	@Mock
	Simulation simulation;

	@Test
	public void testStep() {

		DataRecorder recorder = new DataRecorder(4);
		double[] averageCooperation = new double[4];
		double[] averagePayoff = new double[4];

		int round = 0;
		averageCooperation[round] = 0.5;
		averagePayoff[round] = 3.5;
		Mockito.when(simulation.getCooperationValue()).thenReturn(averageCooperation[round]);
		Mockito.when(simulation.getAveragePayoff()).thenReturn(averagePayoff[round]);

		recorder.step(simulation);
		assertEquals(1, recorder.getCooperationValues().size());
		assertEquals(1, recorder.getPayoffValues().size());
		assertEquals(averageCooperation[round], recorder.getCooperationValues().get(0), 0.01);
		assertEquals(averagePayoff[round], recorder.getPayoffValues().get(0), 0.01);

		round = 1;
		averageCooperation[round] = 1.5;
		averagePayoff[round] = 2.3;
		Mockito.when(simulation.getCooperationValue()).thenReturn(averageCooperation[round]);
		Mockito.when(simulation.getAveragePayoff()).thenReturn(averagePayoff[round]);

		recorder.step(simulation);
		assertEquals(2, recorder.getCooperationValues().size());
		assertEquals(2, recorder.getPayoffValues().size());
		assertEquals(averageCooperation[0], recorder.getCooperationValues().get(0), 0.01);
		assertEquals(averagePayoff[0], recorder.getPayoffValues().get(0), 0.01);
		assertEquals(averageCooperation[1], recorder.getCooperationValues().get(1), 0.01);
		assertEquals(averagePayoff[1], recorder.getPayoffValues().get(1), 0.01);

	}

	@Test
	public void getAverage() {

		List<Double> list = new ArrayList<>();
		list.add(0.3);
		list.add(0.5);
		list.add(0.8);
		list.add(0.2);
		list.add(0.3);
		list.add(0.4);
		DataRecorder recorder = new DataRecorder();
		double result = recorder.getAverage(list, 3);
		assertEquals(0.3, result, 0.01);
	}
	
	@Test public void getArray() {
		
		List<Double> list = new ArrayList<>();
		list.add(0.3);
		list.add(0.5);
		list.add(0.8);
		list.add(0.2);
		list.add(0.3);
		list.add(0.4);
		DataRecorder recorder = new DataRecorder();
		double[] result = recorder.getArray(list, 3);
		assertEquals(3, result.length);
		assertEquals(0.4, result[0], 0.01);
	}
	
	@Test
	public void isSteadyTrue() {
		
		List<Double> list = new ArrayList<>();
		list.add(0.1);
		list.add(0.1);
		list.add(0.11);
		list.add(0.1);
		list.add(0.09);
		list.add(0.1);
		DataRecorder recorder = new DataRecorder(list, list);
		boolean result = recorder.isSteady(5, 0.1);
		assertEquals(true, result);
	}
	
	@Test
	public void isSteadyFalse() {
		
		List<Double> list = new ArrayList<>();
		list.add(0.1);
		list.add(0.8);
		list.add(0.11);
		list.add(0.7);
		list.add(0.09);
		list.add(0.1);
		DataRecorder recorder = new DataRecorder(list, list);
		boolean result = recorder.isSteady(5, 0.1);
		assertEquals(false, result);
	}
	
	
	@Test
	public void getCooperativity() {

		List<Double> list = new ArrayList<>();
		list.add(0.3);
		list.add(0.5);
		list.add(0.8);
		list.add(0.2);
		list.add(0.3);
		list.add(0.4);
		DataRecorder recorder = new DataRecorder(list, list);
		double result = recorder.getAverageCooperativity(3);
		assertEquals(0.3, result, 0.01);
		
		result = recorder.getAverageCooperativity(1);
		assertEquals(0.4, result, 0.01);
		
		result = recorder.getAverageCooperativity(2);
		assertEquals(0.35, result, 0.01);
		
		result = recorder.getAverageCooperativity(6);
		assertEquals(0.416, result, 0.01);
		
		result = recorder.getAverageCooperativity(9);
		assertEquals(0.416, result, 0.01);
	}

	@Test
	public void testClear() {

		DataRecorder recorder = new DataRecorder(10);
		Mockito.when(simulation.getCooperationValue()).thenReturn(1.1);
		Mockito.when(simulation.getAveragePayoff()).thenReturn(1.1);

		recorder.step(simulation);
		recorder.step(simulation);
		recorder.step(simulation);
		recorder.step(simulation);
		recorder.step(simulation);
		assertEquals(5, recorder.getCooperationValues().size());
		assertEquals(5, recorder.getPayoffValues().size());

		recorder.clear();
		assertEquals(0, recorder.getCooperationValues().size());
		assertEquals(0, recorder.getPayoffValues().size());
	}

}
