package i5.las2peer.services.ocd.cooperation.simulation.dynamic;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import i5.las2peer.services.ocd.cooperation.simulation.dynamic.Dynamic;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.DynamicFactory;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.DynamicType;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.Moran;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.Replicator;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.UnconditionalImitation;
import i5.las2peer.services.ocd.cooperation.simulation.dynamic.WinStayLoseShift;

public class DynamicFactoryTest {
	
	@Rule
    public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void buildUnkown() {
		
		thrown.expect(IllegalArgumentException.class);		
		DynamicFactory factory = new DynamicFactory();
		factory.build(DynamicType.UNKNOWN, new double[]{1.5});				
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
		
		thrown.expect(IllegalArgumentException.class);	
		
		DynamicFactory factory = new DynamicFactory();
		DynamicType type = DynamicType.REPLICATOR;
		factory.build(type, new double[]{});
		
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
