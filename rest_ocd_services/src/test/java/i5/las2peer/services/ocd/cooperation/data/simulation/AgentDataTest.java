package i5.las2peer.services.ocd.cooperation.data.simulation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;


public class AgentDataTest {

	@Test
	public void setStrategies() {
		
		boolean strategy0 = true;
		boolean strategy1 = false;
		boolean strategy2 = true;
		
		AgentData agentData = new AgentData();
		List<Boolean> list = new ArrayList<>(3);
		list.add(strategy0);
		list.add(strategy1);
		list.add(strategy2);
		
		agentData.setStrategies(list);
		List<Boolean> results = agentData.getStrategies();
		assertEquals(list.size(), results.size());
		assertEquals(strategy0, results.get(0));
		assertEquals(strategy1, results.get(1));
		assertEquals(strategy2, results.get(2));
		
		boolean result = agentData.getFinalStrategy();
		assertEquals(strategy2, result);
	}
	
	@Test
	public void setPayoff() {
		
		double payoff0 = 3.2;
		double payoff1 = 5.3;
		double payoff2 = 2.0;		
		
		AgentData agentData = new AgentData();
		List<Double> list = new ArrayList<>(3);
		list.add(payoff0);
		list.add(payoff1);
		list.add(payoff2);
		
		agentData.setPayoff(list);
		List<Double> results = agentData.getPayoff();
		assertEquals(list.size(), results.size());
		assertEquals(payoff0, results.get(0), 0.01);
		assertEquals(payoff1, results.get(1), 0.01);
		assertEquals(payoff2, results.get(2), 0.01);
		
		double result = agentData.getFinalPayoff();
		assertEquals(payoff2, result, 0.01);

	}
	
}
