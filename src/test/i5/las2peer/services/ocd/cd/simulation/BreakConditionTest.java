package i5.las2peer.services.ocd.cd.simulation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import i5.las2peer.services.ocd.cd.simulation.Agent;
import i5.las2peer.services.ocd.cd.simulation.BreakCondition;
import i5.las2peer.services.ocd.cd.simulation.DataRecorder;
import i5.las2peer.services.ocd.cd.simulation.Simulation;
import sim.engine.Schedule;
import sim.engine.Stoppable;
import sim.util.Bag;

@RunWith(MockitoJUnitRunner.class)
public class BreakConditionTest {
	
	@Spy
	BreakCondition condition;
	
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

	@Before
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
	
		BreakCondition condition = new BreakCondition();
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

		BreakCondition condition = new BreakCondition();
		boolean result = condition.isFullfilled(simulation);
		assertEquals(true, result);
	}

	@Test
	public void isBreakConditionSteadyStateTrue() {
		
		BreakCondition condition = new BreakCondition();
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
		
		BreakCondition condition = new BreakCondition();
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

		Mockito.when(simulation.getRound()).thenReturn(minIterations - 1);

		Mockito.when(agent0.isSteady()).thenReturn(true);
		Mockito.when(agent1.isSteady()).thenReturn(true);
		Mockito.when(agent2.isSteady()).thenReturn(true);
		Mockito.when(agent3.isSteady()).thenReturn(true);

		BreakCondition condition = new BreakCondition();
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
