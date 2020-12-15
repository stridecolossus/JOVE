package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class InterpolatorTest {
	@Test
	void linear() {
		assertEquals(0.0f, Interpolator.LINEAR.interpolate(0.0f));
		assertEquals(0.5f, Interpolator.LINEAR.interpolate(0.5f));
		assertEquals(1.0f, Interpolator.LINEAR.interpolate(1.0f));
	}

	@Test
	void quadratic() {
		assertEquals(0.0f, Interpolator.QUADRATIC.interpolate(0.0f));
		assertEquals(0.25f, Interpolator.QUADRATIC.interpolate(0.5f));
		assertEquals(1.0f, Interpolator.QUADRATIC.interpolate(1.0f));
	}

	@Test
	void cubic() {
		assertEquals(0.0f, Interpolator.CUBIC.interpolate(0.0f));
		assertEquals(0.125f, Interpolator.CUBIC.interpolate(0.5f));
		assertEquals(1.0f, Interpolator.CUBIC.interpolate(1.0f));
	}

	@Test
	void pow() {
		final Interpolator squared = Interpolator.pow(2);
		assertEquals(0.0f, squared.interpolate(0.0f));
		assertEquals(0.25f, squared.interpolate(0.5f));
		assertEquals(1.0f, squared.interpolate(1.0f));
	}

	@Test
	void flip() {
		final Interpolator flip = Interpolator.flip(Interpolator.LINEAR);
		assertEquals(1.0f, flip.interpolate(0.0f));
		assertEquals(0.5f, flip.interpolate(0.5f));
		assertEquals(0.0f, flip.interpolate(1.0f));
	}

	@Test
	void scale() {
		final Interpolator flip = Interpolator.scale(Interpolator.LINEAR);
		assertEquals(0.0f, flip.interpolate(0.0f));
		assertEquals(0.25f, flip.interpolate(0.5f));
		assertEquals(1.0f, flip.interpolate(1.0f));
	}

	@Test
	void smooth() {
		// TODO - weak
		assertEquals(0.0f, Interpolator.SMOOTH.interpolate(0.0f));
		assertEquals(0.5f, Interpolator.SMOOTH.interpolate(0.5f));
		assertEquals(1.0f, Interpolator.SMOOTH.interpolate(1.0f));
	}

	@Test
	void mix() {
		final Interpolator stop = Interpolator.of(Interpolator.FLIP, Interpolator.QUADRATIC, Interpolator.FLIP);
		System.out.println(stop.interpolate(0));
		System.out.println(stop.interpolate(0.25f));
		System.out.println(stop.interpolate(0.5f));
		System.out.println(stop.interpolate(0.75f));
		System.out.println(stop.interpolate(1));
		//Interpolator.mix(Interpolator.QUADRATIC, stop, weight)
	}

	@Test
	void linearFloat() {
		final Interpolator lerp = Interpolator.lerp(1, 3);
		assertEquals(1, lerp.interpolate(0.0f));
		assertEquals(2, lerp.interpolate(0.5f));
		assertEquals(3, lerp.interpolate(1.0f));
	}

	@Test
	void lerp() {
		assertEquals(1, Interpolator.lerp(1, 3, 0.0f));
		assertEquals(2, Interpolator.lerp(1, 3, 0.5f));
		assertEquals(3, Interpolator.lerp(1, 3, 1.0f));
	}
}
