package i5.las2peer.services.ocd.cooperation.simulation.dynamic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;


public class DynamicFactoryTest {

	@Test
	public void buildUnkown() {

		DynamicFactory factory = new DynamicFactory();
		assertThrows(IllegalArgumentException.class, () -> {
			factory.build(DynamicType.UNKNOWN, new double[]{1.5});
		});
	}
	
	@Test
	public void buildReplicator() {
		
		DynamicFactory factory = new DynamicFactory();
		double value = 0.7;
		DynamicType type = DynamicType.REPLICATOR;
		Dynamic dynamic = factory.build(type, 0.7);
		assertEquals(Replicator.class, dynamic.getClass());
		assertEquals(value, dynamic.getValues()[0], 0.01);		
	}
	
	@Test
	public void buildReplicatorIllegalValues() {
		

		DynamicFactory factory = new DynamicFactory();
		DynamicType type = DynamicType.REPLICATOR;
		assertThrows(IllegalArgumentException.class, () -> {
			factory.build(type, new double[]{});
		});
	}
	
	@Test
	public void buildUnconditionalImitation() {
		
		DynamicFactory factory = new DynamicFactory();
		DynamicType type = DynamicType.UNCONDITIONAL_IMITATION;
		Dynamic dynamic = factory.build(type, 0.7);
		assertEquals(UnconditionalImitation.class, dynamic.getClass());	
	}
	
	@Test
	public void buildMoran() {
		
		DynamicFactory factory = new DynamicFactory();
		DynamicType type = DynamicType.MORAN;
		Dynamic dynamic = factory.build(type, 0.7);
		assertEquals(Moran.class, dynamic.getClass());	
	}
	
	@Test
	public void buildWS_LS() {
		
		DynamicFactory factory = new DynamicFactory();
		DynamicType type = DynamicType.WS_LS;
		Dynamic dynamic = factory.build(type, 0.7);
		assertEquals(WinStayLoseShift.class, dynamic.getClass());	
	}
	


	
}
