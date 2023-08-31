package i5.las2peer.services.ocd.cooperation.simulation.termination;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;

import i5.las2peer.services.ocd.cooperation.simulation.Agent;
import i5.las2peer.services.ocd.cooperation.simulation.DataRecorder;
import i5.las2peer.services.ocd.cooperation.simulation.Simulation;
import sim.engine.Schedule;
import sim.engine.Stoppable;
import sim.util.Bag;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StationaryStateConditionTest {
	
	@Spy
	StationaryStateCondition condition;
	
	@Mock
	Simulation simulation;
	
	@Mock
	DataRecorder recorder;

	@Mock
	Agent agent0;
	@Mock
	Agent agent1;
	@Mock
	Agent agent2;
	@Mock
	Agent agent3;
	
	@Mock
	Schedule schedule = new Schedule();
	
	@Spy
	Stoppable stoppable1 = schedule.scheduleRepeating(new Agent());
	@Spy
	Stoppable stoppable2 = schedule.scheduleRepeating(new Agent());

	int minIterations = 1000;
	int maxIterations = 10000;

	@BeforeEach
	public void setUp() {

		Bag agentBag = new Bag();
		agentBag.add(agent0);
		agentBag.add(agent1);
		agentBag.add(agent2);
		agentBag.add(agent3);

		Mockito.when(simulation.getAgents()).thenReturn(agentBag);		
		Mockito.when(simulation.getDataRecorder()).thenReturn(recorder);
		
		condition.setMaxIterations(maxIterations);
		condition.setMinIterations(minIterations);
	}
	
	@Test
	public void isBreakConditionFalse() {

		Mockito.when(simulation.getRound()).thenReturn(maxIterations - 4);
	
		StationaryStateCondition condition = new StationaryStateCondition();
		condition.setMaxIterations(10000);
		condition.setMinIterations(1000);
		condition.setWindow(200);
		condition.setThreshold(0.1);		
		Mockito.when(recorder.isSteady(200, 0.1)).thenReturn(true);	
		
		Mockito.when(simulation.getRound()).thenReturn(1111);
		boolean result = condition.isFullfilled(simulation);
		assertEquals(false, result);
	}
	
	@Test
	public void isBreakConditionMaxRoundTrue() {

		Mockito.when(simulation.getRound()).thenReturn(maxIterations + 2);

		StationaryStateCondition condition = new StationaryStateCondition();
		boolean result = condition.isFullfilled(simulation);
		assertEquals(true, result);
	}

	@Test
	public void isBreakConditionSteadyStateTrue() {
		
		StationaryStateCondition condition = new StationaryStateCondition();
		condition.setMaxIterations(10000);
		condition.setMinIterations(1000);
		condition.setWindow(200);
		condition.setThreshold(0.1);		
		Mockito.when(recorder.isSteady(200, 0.1)).thenReturn(true);	
		
		System.out.println(condition.getMinIterations());
		System.out.println(condition.getThreshold());
		System.out.println(condition.getWindow());
		
		Mockito.when(simulation.getRound()).thenReturn(999);
		boolean result = condition.isFullfilled(simulation);
		assertEquals(true, result);
		
		Mockito.when(simulation.getRound()).thenReturn(1199);
		result = condition.isFullfilled(simulation);
		assertEquals(true, result);
	}
	
	@Test
	public void isBreakConditionSteadyStateFalse() {
		
		StationaryStateCondition condition = new StationaryStateCondition();
		condition.setMaxIterations(10000);
		condition.setMinIterations(1000);
		condition.setWindow(200);
		condition.setThreshold(0.1);		
		Mockito.when(recorder.isSteady(200, 0.1)).thenReturn(false);	
		
		Mockito.when(simulation.getRound()).thenReturn(999);			
		boolean result = condition.isFullfilled(simulation);
		assertEquals(false, result);
		
		Mockito.when(simulation.getRound()).thenReturn(1199);
		result = condition.isFullfilled(simulation);
		assertEquals(false, result);
	}

	@Test
	public void isBreakConditionMinRoundFalse() {

		Mockito.when(simulation.getRound()).thenReturn(50);

		Mockito.when(agent0.isSteady()).thenReturn(true);
		Mockito.when(agent1.isSteady()).thenReturn(true);
		Mockito.when(agent2.isSteady()).thenReturn(true);
		Mockito.when(agent3.isSteady()).thenReturn(true);

		StationaryStateCondition condition = new StationaryStateCondition();
		condition.setMinIterations(100);
		condition.setMaxIterations(200);
		condition.setWindow(100);
		boolean result = condition.isFullfilled(simulation);
		assertEquals(false, result);
	}
	
	@Test
	public void stepTestConditionFalse() {

		Mockito.when(condition.isFullfilled(simulation)).thenReturn(false);		
		condition.step(simulation);		
		Mockito.verify(condition, Mockito.atLeastOnce()).isFullfilled(simulation);
	}
	
	@Test
	public void stepTestConditionTrue() {

		Mockito.when(condition.isFullfilled(simulation)).thenReturn(true);
		condition.step(simulation);	
		Mockito.verify(condition, Mockito.atLeastOnce()).isFullfilled(simulation);
	}
	
	
	

}
