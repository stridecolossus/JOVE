package org.sarge.jove.scene;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Matrix;

public class OrthographicProjectionTest {
	@Test
	public void getMatrix() {
		final Projection p = new OrthographicProjection();
		final Matrix m = p.getMatrix( 1, 100, new Dimensions( 640, 480 ) );
		assertNotNull( m );
	}
}
