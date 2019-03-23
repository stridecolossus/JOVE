package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MathsUtilTest {
	@Test
	public void equals() {
		assertEquals(true, MathsUtil.equals(42, 42));
		assertEquals(true, MathsUtil.equals(42, 42.0001f));
		assertEquals(false, MathsUtil.equals(42, 999));
		assertEquals(false, MathsUtil.equals(42, 42.01f));
	}

	@Test
	public void isZero() {
		assertEquals(true, MathsUtil.equals(42, 42));
	}

	@Test
	public void isEven() {
		assertEquals(true, MathsUtil.isEven(0));
		assertEquals(false, MathsUtil.isEven(1));
		assertEquals(true, MathsUtil.isEven(2));
	}

	@Test
	public void isPowerOfTwo() {
		assertEquals(false, MathsUtil.isPowerOfTwo(0));
		assertEquals(true, MathsUtil.isPowerOfTwo(1));
		assertEquals(true, MathsUtil.isPowerOfTwo(2));
		assertEquals(false, MathsUtil.isPowerOfTwo(3));
		assertEquals(true, MathsUtil.isPowerOfTwo(4));
		assertEquals(true, MathsUtil.isPowerOfTwo(8));
	}

	@Test
	public void toDegrees() {
		assertEquals(0, MathsUtil.toDegrees(0));
		assertEquals(90, MathsUtil.toDegrees(MathsUtil.HALF_PI));
		assertEquals(180, MathsUtil.toDegrees(MathsUtil.PI));
		assertEquals(360, MathsUtil.toDegrees(MathsUtil.TWO_PI));
	}

	@Test
	public void toRadians() {
		assertEquals(0, MathsUtil.toRadians(0));
		assertEquals(MathsUtil.HALF_PI, MathsUtil.toRadians(90));
		assertEquals(MathsUtil.PI, MathsUtil.toRadians(180));
		assertEquals(MathsUtil.TWO_PI, MathsUtil.toRadians(360));
	}

	@Test
	public void sqrt() {
		assertEquals((float) Math.sqrt(42), MathsUtil.sqrt(42));
	}

	@Test
	public void trigonetry() {
		assertEquals((float) Math.sin(0.5f), MathsUtil.sin(0.5f), 0.0001f);
		assertEquals((float) Math.cos(0.5f), MathsUtil.cos(0.5f), 0.0001f);
		assertEquals((float) Math.tan(0.5f), MathsUtil.tan(0.5f), 0.0001f);
		assertEquals((float) Math.asin(0.5f), MathsUtil.asin(0.5f), 0.0001f);
		assertEquals((float) Math.acos(0.5f), MathsUtil.acos(0.5f), 0.0001f);
		assertEquals((float) Math.atan(0.5f), MathsUtil.atan(0.5f), 0.0001f);
	}
}
