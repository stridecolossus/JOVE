package org.sarge.jove.scene;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Matrix;

public class PerspectiveProjectionTest {
	private Projection proj;

	@Before
	public void before() {
		proj = new PerspectiveProjection();
	}

	@Test
	public void getMatrix() {
		final Matrix m = proj.getMatrix( 0.1f, 1000f, new Dimensions( 640, 480 ) );
		assertNotNull( m );
	}
}
