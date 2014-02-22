package org.sarge.jove.animation;

import static org.sarge.jove.util.MockitoTestCase.assertFloatEquals;

import org.junit.Test;

public class LinearAnimationInterpolatorTest {
	@Test
	public void interpolate() {
		final LinearAnimationInterpolator interpolator = new LinearAnimationInterpolator( 100, 200 );
		assertFloatEquals( 100f, interpolator.interpolate( 0f ) );
		assertFloatEquals( 150f, interpolator.interpolate( 0.5f ) );
		assertFloatEquals( 200f, interpolator.interpolate( 1f ) );
	}
}
