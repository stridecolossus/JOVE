package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.util.MathsUtility.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Cosine.Pair;

class CosineTableTest {
	private CosineTable table;

	@BeforeEach
	void before() {
		table = new CosineTable(360);
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> new CosineTable(0));
		assertThrows(IllegalArgumentException.class, () -> new CosineTable(3));
	}

	@Test
	void table() {
		for(int n = -360; n < +360; ++n) {
			final float angle = toRadians(n);
			assertNotNull(table.cosine(angle));
		}
	}

	@Test
	void cardinal() {
		assertEquals(new Pair(0, +1), table.cosine(0));
		assertEquals(new Pair(+1, 0), table.cosine(HALF_PI));
		assertEquals(new Pair(0, -1), table.cosine(PI));
		assertEquals(new Pair(0, +1), table.cosine(TWO_PI));
	}

	@Test
	void negative() {
		assertEquals(new Pair(-1, 0), table.cosine(-HALF_PI));
		assertEquals(new Pair(0, -1), table.cosine(-PI));
		assertEquals(new Pair(0, +1), table.cosine(-TWO_PI));
	}
}
