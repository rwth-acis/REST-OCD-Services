package i5.las2peer.services.ocd.cd.simulation.dynamic;

import static org.junit.Assert.*;

import org.junit.Test;

import i5.las2peer.services.ocd.cd.simulation.dynamic.Dynamic;
import i5.las2peer.services.ocd.cd.simulation.dynamic.DynamicType;
import i5.las2peer.services.ocd.cd.simulation.dynamic.Moran;
import i5.las2peer.services.ocd.cd.simulation.dynamic.Replicator;
import i5.las2peer.services.ocd.cd.simulation.dynamic.UnconditionalImitation;

public class DynamicTypeTest {
	
	@Test
	public void typeExistsTest() {
		
		boolean result;
		
		result = DynamicType.TypeExists("rePliCator");
		assertEquals(true, result);
		
		result = DynamicType.TypeExists("imitation");
		assertEquals(true, result);
		
		result = DynamicType.TypeExists("was35hkld");
		assertEquals(false, result);
		
	}
	
	@Test
	public void fromStringTest() {
		
		DynamicType result;
		
		result = DynamicType.fromString("RePlicaTor");
		assertEquals(DynamicType.REPLICATOR, result);
		
		result = DynamicType.fromString("Ws_lS");
		assertEquals(DynamicType.WS_LS, result);
		
		result = DynamicType.fromString("sdk239wlk");
		assertEquals(DynamicType.UNKNOWN, result);
				
	}
	
	@Test
	public void getDynamicClass() {
		
		assertEquals(Dynamic.class, DynamicType.UNKNOWN.getDynamicClass());
		assertEquals(Replicator.class, DynamicType.REPLICATOR.getDynamicClass());
		assertEquals(Moran.class, DynamicType.MORAN.getDynamicClass());
		assertEquals(UnconditionalImitation.class, DynamicType.UNCONDITIONAL_IMITATION.getDynamicClass());
	}
	
	@Test
	public void getType() {
		
		assertEquals(DynamicType.UNCONDITIONAL_IMITATION, DynamicType.getType(UnconditionalImitation.class));
		assertEquals(DynamicType.REPLICATOR, DynamicType.getType(Replicator.class));
		assertEquals(DynamicType.MORAN, DynamicType.getType(Moran.class));
		
	}
	
}
