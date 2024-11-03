package org.sarge.jove.scene.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.geometry.Axis.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.*;
import org.sarge.jove.scene.volume.Volume;
import org.sarge.jove.util.MathsUtility;

class FrustumTest {
	private Frustum frustum;
	private Plane plane;

	@BeforeEach
	void before() {
		plane = new Plane(X, -1);
		frustum = new Frustum(new Plane[]{plane});
	}

	@Test
	void constructor() {
		assertEquals(List.of(plane), frustum.planes());
	}

	@Test
	void contains() {
		assertEquals(true, frustum.contains(new Point(2, 0, 0)));
		assertEquals(true, frustum.contains(new Point(1, 0, 0)));
		assertEquals(false, frustum.contains(new Point(0, 0, 0)));
	}

	@Test
	void intersects() {
		final Volume vol = mock(Volume.class);
		when(vol.intersects(plane)).thenReturn(true);
		assertEquals(true, frustum.intersects(vol));
		assertEquals(false, frustum.intersects(mock(Volume.class)));
	}

	@Test
	void equals() {
		assertEquals(true, frustum.equals(frustum));
		assertEquals(true, frustum.equals(new Frustum(new Plane[]{plane})));
		assertEquals(false, frustum.equals(null));
		assertEquals(false, frustum.equals(new Frustum(new Plane[]{new Plane(Y, 1)})));
	}

	@Test
	void extract() {
		// Construct a view matrix
		final Matrix m = new Matrix.Builder(4)
				.identity()
				.row(0, X)
				.row(1, Y)		// Note this test does not invert the Y axis
				.row(2, Z)
				.build();

		// Create frustum
		frustum = Frustum.of(m);
		assertNotNull(frustum);

		// Check planes
		final Plane[] expected = {
				new Plane(Z.invert(), 1),			// Near
				new Plane(Z, 1),					// Far
				new Plane(X, 1),					// Left
				new Plane(X.invert(), 1),			// Right
				new Plane(Y, 1),					// Top
				new Plane(Y.invert(), 1)			// Bottom
		};
		assertArrayEquals(expected, frustum.planes().toArray());

		// Frustum from view matrix should be same as identity
		assertEquals(frustum, Frustum.of(Matrix.IDENTITY));
	}

	@Test
	void extractProjection() {
		final Projection projection = Projection.perspective(MathsUtility.HALF_PI);
		final Matrix m = projection.matrix(0, 1, new Dimensions(1, 1));
		frustum = Frustum.of(m);
		assertNotNull(frustum);
		// TODO
//		System.out.println("proj\n"+m);
	}
}
