package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.geometry.Axis.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Matrix.Matrix4;
import org.sarge.jove.util.MathsUtil;

class FrustumTest {
	private Frustum frustum;
	private Plane plane;

	@BeforeEach
	void before() {
		plane = new Plane(X.vector(), -1);
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
		assertEquals(false, frustum.equals(new Frustum(new Plane[]{new Plane(Y.vector(), 1)})));
	}

	@Test
	void extract() {
		// Construct a view matrix
		final Matrix m = new Matrix.Builder()
				.identity()
				.row(0, X.vector())
				.row(1, Y.vector())		// Note this test does not invert the Y axis
				.row(2, Z.vector())
				.build();

		// Create frustum
		frustum = Frustum.of(m);
		assertNotNull(frustum);

		// Check planes
		final var expected = List.of(
				new Plane(Z.vector().invert(), 1),			// Near
				new Plane(Z.vector(), 1),					// Far
				new Plane(X.vector(), 1),					// Left
				new Plane(X.vector().invert(), 1),			// Right
				new Plane(Y.vector(), 1),					// Top
				new Plane(Y.vector().invert(), 1)			// Bottom
		);
		assertEquals(expected, frustum.planes());

		// Frustum from view matrix should be same as identity
		assertEquals(frustum, Frustum.of(Matrix4.IDENTITY));
	}

	@Test
	void extractProjection() {
		final Projection projection = Projection.perspective(MathsUtil.HALF_PI);
		final Matrix m = projection.matrix(0, 1, new Dimensions(1, 1));
		frustum = Frustum.of(m);
		assertNotNull(frustum);
		// TODO
//		System.out.println("proj\n"+m);
	}
}
