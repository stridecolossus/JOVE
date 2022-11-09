package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.util.MathsUtil.*;

import org.junit.jupiter.api.Test;

class MathsUtilTest {
	@Test
	void equality() {
		assertEquals(true,  isEqual(42, 42));
		assertEquals(true,  isEqual(42, 42.0001f));
		assertEquals(false, isEqual(42, 999));
		assertEquals(false, isEqual(42, 42.01f));
	}

	@Test
	void infinite() {
		assertEquals(true, isEqual(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
		assertEquals(true, isEqual(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));
		assertEquals(true, isEqual(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY));
		assertEquals(true, isEqual(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
	}

	@Test
	void array() {
		assertEquals(true,  isEqual(new float[]{0.5f}, new float[]{0.5f}));
		assertEquals(false, isEqual(new float[]{0.5f}, new float[]{1}));
		assertEquals(false, isEqual(new float[]{0.5f}, new float[]{}));
	}

	@Test
	void zero() {
		assertEquals(true, isZero(0.00001f));
		assertEquals(true, isZero(0));
		assertEquals(true, isZero(-0));
		assertEquals(false, isZero(1));
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
	void root() {
		assertEquals((float) Math.sqrt(42f), MathsUtil.sqrt(42));
	}

	@Test
	void inverse() {
		assertEquals(1 / (float) Math.sqrt(42f), MathsUtil.inverseRoot(42));
	}
}
