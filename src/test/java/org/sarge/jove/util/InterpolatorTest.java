package org.sarge.jove.util;

import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Test;

public class InterpolatorTest {
	@Test
	public void linear() {
		assertFloatEquals( 3f, Interpolator.LINEAR.interpolate( 3, 5, 0f ) );
		assertFloatEquals( 4f, Interpolator.LINEAR.interpolate( 3, 5, 0.5f ) );
		assertFloatEquals( 5f, Interpolator.LINEAR.interpolate( 3, 5, 1f ) );
	}

	@Test
	public void sine() {
		assertFloatEquals( 3f, Interpolator.SINE.interpolate( 3, 5, 0f ) );
		assertFloatEquals( 4f, Interpolator.SINE.interpolate( 3, 5, 0.5f ) );
		assertFloatEquals( 5f, Interpolator.SINE.interpolate( 3, 5, 1f ) );
	}
}
