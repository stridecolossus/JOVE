package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.util.MathsUtil.HALF_PI;
import static org.sarge.jove.util.MathsUtil.PI;
import static org.sarge.jove.util.MathsUtil.TWO_PI;

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
		assertEquals(true,  MathsUtil.isMask(0b101, 0b101));
		assertEquals(true,  MathsUtil.isMask(0b101, 0b100));
		assertEquals(true,  MathsUtil.isMask(0b101, 0b001));
		assertEquals(false, MathsUtil.isMask(0b101, 0b111));
		assertEquals(false, MathsUtil.isMask(0b101, 0b010));
	}

//	@Test
//	void isMaskZero() {
//		assertThrows(IllegalArgumentException.class, () -> MathsUtil.isMask(42, 0));
//	}

	@Test
	void isBit() {
		assertEquals(true,  MathsUtil.isBit(0b101, 0));
		assertEquals(false, MathsUtil.isBit(0b101, 1));
		assertEquals(true,  MathsUtil.isBit(0b101, 2));
		assertEquals(false, MathsUtil.isBit(0b101, 3));
	}

	@Test
	void isBitInvalidIndex() {
		assertThrows(IllegalArgumentException.class, () -> MathsUtil.isBit(42, Integer.SIZE));
	}

	@Test
	void isPowerOfTwo() {
		assertEquals(false, MathsUtil.isPowerOfTwo(0));
		assertEquals(true,  MathsUtil.isPowerOfTwo(1));
		assertEquals(true,  MathsUtil.isPowerOfTwo(2));
		assertEquals(false, MathsUtil.isPowerOfTwo(3));
		assertEquals(true,  MathsUtil.isPowerOfTwo(4));
		assertEquals(true,  MathsUtil.isPowerOfTwo(8));
	}

	@Test
	void clamp() {
		assertEquals(1, MathsUtil.clamp(0, 1, 2));
		assertEquals(1, MathsUtil.clamp(1, 1, 2));
		assertEquals(2, MathsUtil.clamp(2, 1, 2));
		assertEquals(2, MathsUtil.clamp(3, 1, 2));
	}

	@Test
	void toDegrees() {
		assertEquals(0,   MathsUtil.toDegrees(0));
		assertEquals(90,  MathsUtil.toDegrees(HALF_PI));
		assertEquals(180, MathsUtil.toDegrees(PI));
		assertEquals(360, MathsUtil.toDegrees(TWO_PI));
	}

	@Test
	void toRadians() {
		assertEquals(0, MathsUtil.toRadians(0));
		assertEquals(HALF_PI, MathsUtil.toRadians(90));
		assertEquals(PI, MathsUtil.toRadians(180));
		assertEquals(MathsUtil.TWO_PI, MathsUtil.toRadians(360));
	}

	@Test
	void sqrt() {
		assertEquals((float) Math.sqrt(42f), MathsUtil.sqrt(42));
	}

	@Test
	void inverseRoot() {
		assertEquals(1 / (float) Math.sqrt(42f), MathsUtil.inverseRoot(42));
	}

	@Test
	void sin() {
		check(0, MathsUtil.sin(0));
		check(1, MathsUtil.sin(HALF_PI));
		check(0, MathsUtil.sin(PI));
		check(0, MathsUtil.sin(MathsUtil.TWO_PI));
		check(-1, MathsUtil.sin(-HALF_PI));
		check(0, MathsUtil.sin(-PI));
		check(0, MathsUtil.sin(-MathsUtil.TWO_PI));
	}

	@Test
	void cos() {
		check(1, MathsUtil.cos(0));
		check(0, MathsUtil.cos(HALF_PI));
		check(-1, MathsUtil.cos(PI));
		check(1, MathsUtil.cos(MathsUtil.TWO_PI));
		check(0, MathsUtil.cos(-HALF_PI));
		check(-1, MathsUtil.cos(-PI));
		check(1, MathsUtil.cos(-MathsUtil.TWO_PI));
	}

	private static void check(float expected, float actual) {
		assertEquals(expected, actual, 0.00001f);
	}

	// TODO - other trig
}
