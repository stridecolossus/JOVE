package org.sarge.jove.util;

import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Test;

public class InterpolatorTest {
	@Test
	public void linear() {
		assertFloatEquals(42, Interpolator.LINEAR.interpolate(42));
	}
	
	@Test
	public void sine() {
		assertFloatEquals(0, Interpolator.LINEAR.interpolate(0));
	}
	
	@Test
	public void interpolator() {
		final Interpolator interpolator = Interpolator.interpolator(1, 2, Interpolator.LINEAR);
		assertFloatEquals(1f,   interpolator.interpolate(0f));
		assertFloatEquals(1.5f, interpolator.interpolate(0.5f));
		assertFloatEquals(2f,   interpolator.interpolate(1f));
	}
}
