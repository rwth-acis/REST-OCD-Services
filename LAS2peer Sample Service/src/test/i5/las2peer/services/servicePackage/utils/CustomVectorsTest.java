package i5.las2peer.services.servicePackage.utils;

import static org.junit.Assert.*;

import org.junit.Test;
import org.la4j.vector.Vector;
import org.la4j.vector.sparse.CompressedVector;

public class CustomVectorsTest {

	@Test
	public void test() {
		Vector vector = new CompressedVector(5);
		vector.set(1, 1);
		vector.set(2, 2);
		vector.set(3, 3);
		vector.set(4, 4);
		CustomVectors.setEntriesBelowThresholdToZero(vector, 3);
		assertEquals(0, vector.get(0), 0);
		assertEquals(0, vector.get(1), 0);
		assertEquals(0, vector.get(2), 0);
		assertEquals(3, vector.get(3), 0);
		assertEquals(4, vector.get(4), 0);
		System.out.println(vector);
	}

}
