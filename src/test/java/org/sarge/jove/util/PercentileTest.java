package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.util.Percentile.*;

import org.junit.jupiter.api.*;

class PercentileTest {
	@Test
	void constructor() {
		final Percentile percentile = new Percentile(0.5f);
		assertEquals(50, percentile.toInteger());
		assertEquals(Float.hashCode(0.5f), percentile.hashCode());
		assertEquals("50%", percentile.toString());
	}

	@Test
	void invalidFloat() {
		assertThrows(IllegalArgumentException.class, () -> new Percentile(2f));
		assertThrows(IllegalArgumentException.class, () -> new Percentile(-1f));
	}

	@Test
	void invalidInteger() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> Percentile.of(-1));
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> Percentile.of(101));
	}

	@Test
	void constants() {
		assertEquals(0.0f, ZERO.value());
		assertEquals(0.5f, HALF.value());
		assertEquals(1.0f, ONE.value());
	}

	@Test
	void zero() {
		assertEquals(true, ZERO.isZero());
		assertEquals(true, Percentile.of(0).isZero());
		assertEquals(false, HALF.isZero());
		assertEquals(false, ONE.isZero());
	}

	@Test
	void integer() {
		assertEquals(ZERO, Percentile.of(0));
		assertEquals(HALF, Percentile.of(50));
		assertEquals(ONE, Percentile.of(100));
	}

	@Test
	void integerInvalidRange() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> Percentile.of(-1));
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> Percentile.of(101));
	}

	@Nested
	class ParseTests {
		@Test
		void zero() {
			assertEquals(ZERO, Percentile.parse("0"));
			assertEquals(ZERO, Percentile.parse("0.0"));
		}

		@Test
		void half() {
			assertEquals(HALF, Percentile.parse("0.5"));
			assertEquals(HALF, Percentile.parse("50"));
		}

		@Test
		void one() {
			assertEquals(ONE, Percentile.parse("1.0"));
			assertEquals(ONE, Percentile.parse("100"));
		}

		@Test
		void invalidFloatingPoint() {
			assertThrows(NumberFormatException.class, () -> Percentile.parse("NaN"));
		}

		@Test
		void invalid() {
			assertThrows(NumberFormatException.class, () -> Percentile.parse("cobblers"));
		}
	}

	@Test
	void compare() {
		assertEquals(+1, HALF.compareTo(ZERO));
		assertEquals(0,  HALF.compareTo(HALF));
		assertEquals(-1, HALF.compareTo(ONE));
	}

	@Test
	void lessThan() {
		assertEquals(true, HALF.isLessThan(ONE));
		assertEquals(false, ONE.isLessThan(HALF));
		assertEquals(false, ONE.isLessThan(ONE));
	}

	@Test
	void min() {
		assertEquals(ZERO, HALF.min(ZERO));
		assertEquals(HALF, HALF.min(HALF));
		assertEquals(HALF,  HALF.min(ONE));
	}

	@Test
	void max() {
		assertEquals(HALF, HALF.max(ZERO));
		assertEquals(HALF, HALF.max(HALF));
		assertEquals(ONE,  HALF.max(ONE));
	}

	@Test
	void multiply() {
		assertEquals(ONE, ONE.multiply(ONE));
		assertEquals(Percentile.of(25), HALF.multiply(HALF));
		assertEquals(ZERO, ONE.multiply(ZERO));
	}

	@Test
	void invert() {
		assertEquals(ONE,  ZERO.invert());
		assertEquals(HALF, HALF.invert());
		assertEquals(ZERO, ONE.invert());
	}

	@Test
	void equals() {
		assertEquals(HALF, HALF);
		assertEquals(HALF, new Percentile(0.5f));
		assertNotEquals(HALF, null);
		assertNotEquals(HALF, ONE);
	}
}
