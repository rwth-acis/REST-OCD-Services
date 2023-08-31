package i5.las2peer.services.ocd.cooperation.simulation.game;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class GameFactoryTest {
		
	@Test
	public void buildStandart() {
		
		GameFactory factory = new GameFactory();	
		
		double payoffAA = 0.0;
		double payoffAB = 1.0;
		double payoffBA = 2.0;
		double payoffBB = 3.0;
		
		Game game = factory.build(payoffAA, payoffAB, payoffBA, payoffBB);
		
		assertEquals(Game.class, game.getClass());
		assertEquals(payoffAA, game.getPayoffAA(), 0.01);
		assertEquals(payoffAB, game.getPayoffAB(), 0.01);
		assertEquals(payoffBA, game.getPayoffBA(), 0.01);
		assertEquals(payoffBB, game.getPayoffBB(), 0.01);
		
	}
	
	@Test
	public void buildCostPD() {
		
		GameFactory factory = new GameFactory();	
		
		double cost = 1;
		double benefit = 2;
		
		Game game = factory.build(GameType.PRISONERS_DILEMMA, cost, benefit);
		
		assertEquals(Game.class, game.getClass());
		assertEquals(1, game.getPayoffAA(), 0.01);
		assertEquals(-1, game.getPayoffAB(), 0.01);
		assertEquals(2, game.getPayoffBA(), 0.01);
		assertEquals(0, game.getPayoffBB(), 0.01);		
	}
	
	@Test
	public void buildCostSD() {
		
		GameFactory factory = new GameFactory();	
		
		double cost = 1;
		double benefit = 2;
		
		Game game = factory.build(GameType.CHICKEN, cost, benefit);
		
		assertEquals(Game.class, game.getClass());
		assertEquals(1.5, game.getPayoffAA(), 0.01);
		assertEquals(1, game.getPayoffAB(), 0.01);
		assertEquals(2, game.getPayoffBA(), 0.01);
		assertEquals(0, game.getPayoffBB(), 0.01);
		
	}
	
	
	
}
