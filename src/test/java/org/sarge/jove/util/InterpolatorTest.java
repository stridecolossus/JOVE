package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class InterpolatorTest {
	@Test
	void none() {
		assertEquals(1, Interpolator.NONE.interpolate(1));
	}

	@Test
	void invert() {
		assertEquals(0, Interpolator.INVERT.interpolate(1));
	}

	@Test
	void cosine() {
		assertEquals(0, Interpolator.COSINE.interpolate(0));
		assertEquals(0.5f, Interpolator.COSINE.interpolate(0.5f));
		assertEquals(1, Interpolator.COSINE.interpolate(1));
	}

	@Test
	void smooth() {
		assertEquals(0, Interpolator.SMOOTH.interpolate(0));
		assertEquals(0.5f, Interpolator.SMOOTH.interpolate(0.5f));
		assertEquals(1, Interpolator.SMOOTH.interpolate(1));
	}

	@Test
	void square() {
		assertEquals(0, Interpolator.SQUARED.interpolate(0));
		assertEquals(0.25f, Interpolator.SQUARED.interpolate(0.5f));
		assertEquals(1, Interpolator.SQUARED.interpolate(1));
	}

	@Test
	void exponential() {
		final Interpolator exp = Interpolator.exponent(3);
		assertNotNull(exp);
		assertEquals(0, exp.interpolate(0));
		assertEquals(0.125f, exp.interpolate(0.5f));
		assertEquals(1, exp.interpolate(1));
	}

	@Test
	void linear() {
		final Interpolator linear = Interpolator.linear(1, 2);
		assertNotNull(linear);
		assertEquals(1, linear.interpolate(0));
		assertEquals(1.5f, linear.interpolate(0.5f));
		assertEquals(2, linear.interpolate(1));
	}

	@Test
	void of() {
		final Interpolator compound = Interpolator.of(1, 2, Interpolator.SQUARED);
		assertNotNull(compound);
		assertEquals(1, compound.interpolate(0));
		assertEquals(1.25f, compound.interpolate(0.5f));
		assertEquals(2, compound.interpolate(1));
	}

	@Test
	void lerp() {
		assertEquals(1, Interpolator.lerp(1, 2, 0));
		assertEquals(1.5f, Interpolator.lerp(1, 2, 0.5f));
		assertEquals(2, Interpolator.lerp(1, 2, 1));
	}
}
