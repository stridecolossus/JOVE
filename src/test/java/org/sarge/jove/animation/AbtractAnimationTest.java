package org.sarge.jove.animation;

import static org.junit.Assert.assertEquals;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import org.junit.Before;
import org.junit.Test;

public class AbtractAnimationTest {
	private AbstractAnimation animation;

	@Before
	public void before() {
		animation = new AbstractAnimation( 5000, 2, 3 ) {
			@Override
			public void update( float pos ) {
				// Does nowt
			}
		};
	}

	@Test
	public void constructor() {
		assertEquals( 5000L, animation.getDuration() );
		assertFloatEquals( 2f, animation.getMinimum() );
		assertFloatEquals( 3f, animation.getMaximum() );
	}
}
