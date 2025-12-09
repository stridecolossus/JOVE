package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.util.MathsUtility.*;

import org.junit.jupiter.api.Test;

class MathsUtilityTest {
	public static void assertApproxEquals(float expected, float actual) {
		if(!isApproxEqual(expected, actual)) {
			fail(String.format("Not approximately equal: expected=%f, actual=%f", expected, actual));
		}
	}

	@Test
	void equality() {
		assertApproxEquals(42, 42);
		assertApproxEquals(42, 42.0001f);
		assertFalse(isApproxEqual(42, 999));
		assertFalse(isApproxEqual(42, 42.01f));
	}

	@Test
	void infinite() {
		assertApproxEquals(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		assertApproxEquals(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		assertApproxEquals(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY);
		assertApproxEquals(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
	}

	@Test
	void zero() {
		assertEquals(true, isApproxZero(0.00001f));
		assertEquals(true, isApproxZero(0));
		assertEquals(true, isApproxZero(-0));
		assertEquals(false, isApproxZero(1));
	}

	@Test
	void powerOfTwo() {
		assertEquals(false, isPowerOfTwo(0));
		assertEquals(true,  isPowerOfTwo(1));
		assertEquals(true,  isPowerOfTwo(2));
		assertEquals(false, isPowerOfTwo(3));
		assertEquals(true,  isPowerOfTwo(4));
		assertEquals(true,  isPowerOfTwo(8));
	}

	@Test
	void clamping() {
		assertEquals(1, clamp(0, 1, 2));
		assertEquals(1, clamp(1, 1, 2));
		assertEquals(2, clamp(2, 1, 2));
		assertEquals(2, clamp(3, 1, 2));
	}

	@Test
	void saturation() {
		assertEquals(0, saturate(-1));
		assertEquals(0, saturate(0));
		assertEquals(HALF, saturate(HALF));
		assertEquals(1, saturate(1));
		assertEquals(1, saturate(2));
	}

	@Test
	void root() {
		assertApproxEquals((float) Math.sqrt(42f), MathsUtility.sqrt(42));
	}

	@Test
	void inverse() {
		assertApproxEquals(1 / (float) Math.sqrt(42f), MathsUtility.inverseSquareRoot(42));
	}

	@Test
	void format() {
		assertEquals("0.5, 3.1416", MathsUtility.toString(HALF, PI));
	}
}
