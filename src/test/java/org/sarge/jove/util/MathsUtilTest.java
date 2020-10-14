package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class MathsUtilTest {
	@Test
	void isEqual() {
		assertEquals(true, MathsUtil.isEqual(42, 42));
		assertEquals(true, MathsUtil.isEqual(42, 42.0001f));
		assertEquals(false, MathsUtil.isEqual(42, 999));
		assertEquals(false, MathsUtil.isEqual(42, 42.01f));
	}

	@Test
	void isArrayEqual() {
		assertEquals(true, MathsUtil.isEqual(new float[]{0.5f}, new float[]{0.5f}));
		assertEquals(false, MathsUtil.isEqual(new float[]{0.5f}, new float[]{1}));
		assertEquals(false, MathsUtil.isEqual(new float[]{0.5f}, new float[]{}));
	}

	@Test
	void isZero() {
		assertEquals(true, MathsUtil.isZero(0.00001f));
		assertEquals(true, MathsUtil.isZero(0));
		assertEquals(true, MathsUtil.isZero(-0));
		assertEquals(false, MathsUtil.isZero(MathsUtil.ACCURACY));
		assertEquals(false, MathsUtil.isZero(1));
	}

	@Test
	void isEven() {
		assertEquals(true, MathsUtil.isEven(0));
		assertEquals(false, MathsUtil.isEven(1));
		assertEquals(true, MathsUtil.isEven(2));
	}

	@Test
	void isMask() {
		assertEquals(true, MathsUtil.isMask(0b101, 0b101));
		assertEquals(true, MathsUtil.isMask(0b101, 0b100));
		assertEquals(true, MathsUtil.isMask(0b101, 0b001));
		assertEquals(false, MathsUtil.isMask(0b101, 0b111));
		assertEquals(false, MathsUtil.isMask(0b101, 0b010));
	}

	@Test
	void isMaskZero() {
		assertThrows(IllegalArgumentException.class, () -> MathsUtil.isMask(42, 0));
	}

	@Test
	void isBit() {
		assertEquals(true, MathsUtil.isBit(0b101, 0));
		assertEquals(false, MathsUtil.isBit(0b101, 1));
		assertEquals(true, MathsUtil.isBit(0b101, 2));
		assertEquals(false, MathsUtil.isBit(0b101, 3));
	}

	@Test
	void isBitInvalidIndex() {
		assertThrows(IllegalArgumentException.class, () -> MathsUtil.isBit(42, Integer.SIZE));
	}

	@Test
	void isPowerOfTwo() {
		assertEquals(false, MathsUtil.isPowerOfTwo(0));
		assertEquals(true, MathsUtil.isPowerOfTwo(1));
		assertEquals(true, MathsUtil.isPowerOfTwo(2));
		assertEquals(false, MathsUtil.isPowerOfTwo(3));
		assertEquals(true, MathsUtil.isPowerOfTwo(4));
		assertEquals(true, MathsUtil.isPowerOfTwo(8));
	}

	@Test
	void toDegrees() {
		assertEquals(0, MathsUtil.toDegrees(0));
		assertEquals(90, MathsUtil.toDegrees(MathsUtil.HALF_PI));
		assertEquals(180, MathsUtil.toDegrees(MathsUtil.PI));
		assertEquals(360, MathsUtil.toDegrees(MathsUtil.TWO_PI));
	}

	@Test
	void toRadians() {
		assertEquals(0, MathsUtil.toRadians(0));
		assertEquals(MathsUtil.HALF_PI, MathsUtil.toRadians(90));
		assertEquals(MathsUtil.PI, MathsUtil.toRadians(180));
		assertEquals(MathsUtil.TWO_PI, MathsUtil.toRadians(360));
	}

	@Test
	void sqrt() {
		assertEquals((float) Math.sqrt(42), MathsUtil.sqrt(42));
	}

	@Test
	void trigonetry() {
		assertEquals((float) Math.sin(0.5f), MathsUtil.sin(0.5f), 0.0001f);
		assertEquals((float) Math.cos(0.5f), MathsUtil.cos(0.5f), 0.0001f);
		assertEquals((float) Math.tan(0.5f), MathsUtil.tan(0.5f), 0.0001f);
		assertEquals((float) Math.asin(0.5f), MathsUtil.asin(0.5f), 0.0001f);
		assertEquals((float) Math.acos(0.5f), MathsUtil.acos(0.5f), 0.0001f);
		assertEquals((float) Math.atan(0.5f), MathsUtil.atan(0.5f), 0.0001f);
	}
}
