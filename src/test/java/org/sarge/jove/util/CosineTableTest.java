package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.util.Trigonometric.*;

import org.junit.jupiter.api.*;

class CosineTableTest {
	private static final int CIRCLE = 360;
	private static final float ACCURACY = 0.025f;

	private CosineTable table;

	@BeforeEach
	void before() {
		table = new CosineTable(1024);
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> new CosineTable(0));
		assertThrows(IllegalArgumentException.class, () -> new CosineTable(3));
	}

	@Test
	void cos() {
		for(int n = -CIRCLE; n < 2 * CIRCLE; ++n) {
			final float angle = toRadians(n);
			final float expected = (float) Math.cos(angle);
			final float actual = table.cos(angle);
			assertEquals(expected, actual, ACCURACY);
		}
	}

	@Test
	void cardinal() {
		assertEquals(1, table.cos(0));
		assertEquals(0, table.cos(HALF_PI));
		assertEquals(-1, table.cos(PI));
		assertEquals(0, table.cos(PI + HALF_PI));
		assertEquals(1, table.cos(TWO_PI));
	}

	@Test
	void sin() {
		for(int n = -CIRCLE; n < 2 * CIRCLE; ++n) {
			final float angle = toRadians(n);
			final float expected = (float) Math.sin(angle);
			final float actual = table.sin(angle);
			assertEquals(expected, actual, ACCURACY);
		}
	}
}
