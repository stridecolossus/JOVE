package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.util.Interpolator.*;
import static org.sarge.jove.util.MathsUtility.HALF;

import org.junit.jupiter.api.Test;

class InterpolatorTest {
	@Test
	void identity() {
		assertEquals(HALF, IDENTITY.interpolate(HALF));
	}

	//@Test
	void inverse() {
		final var inverse = IDENTITY.invert();
		assertEquals(1, inverse.interpolate(0));
		assertEquals(HALF, inverse.interpolate(HALF));
		assertEquals(0, inverse.interpolate(1));
	}

	@Test
	void linear() {
		final var linear = Interpolator.linear(2, 4);
		assertEquals(2, linear.interpolate(0));
		assertEquals(3, linear.interpolate(HALF));
		assertEquals(4, linear.interpolate(1));
	}

	@Test
	void quadratic() {
		assertEquals(0, QUADRATIC.interpolate(0));
		assertEquals(0.25f, QUADRATIC.interpolate(HALF));
		assertEquals(1, QUADRATIC.interpolate(1));
	}

	@Test
	void exponential() {
		final Interpolator exponential = Interpolator.exponential(2);
		assertEquals(0, exponential.interpolate(0));
		assertEquals(0.25f, exponential.interpolate(HALF));
		assertEquals(1, exponential.interpolate(1));
	}

	@Test
	void smooth() {
		assertEquals(0, SMOOTH.interpolate(0));
		assertEquals(HALF, SMOOTH.interpolate(HALF));
		assertEquals(1, SMOOTH.interpolate(1));
	}
	// TODO - what does this actually test

	@Test
	void mix() {
		final var mix = Interpolator.mix(IDENTITY, IDENTITY, Percentile.HALF);
		assertEquals(0, mix.interpolate(0));
		assertEquals(HALF, mix.interpolate(HALF));
		assertEquals(1, mix.interpolate(1));
	}
	// TODO - what does this actually test
}
