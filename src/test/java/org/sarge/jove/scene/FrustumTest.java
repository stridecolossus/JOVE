package org.sarge.jove.scene;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Plane;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class FrustumTest {
	private Frustum frustum;

	@BeforeEach
	public void before() {
		frustum = new Frustum(new Plane[]{new Plane(Vector.X_AXIS, -1)});
	}

	@Test
	public void constructor() {
		assertNotNull(frustum.planes());
		assertEquals(1, frustum.planes().length);
	}

	@Test
	public void contains() {
		assertEquals(true, frustum.contains(new Point(0, 0, 0)));
		assertEquals(true, frustum.contains(new Point(-1, 0, 0)));
		assertEquals(false, frustum.contains(new Point(-2, 0, 0)));
	}

//	@Test
//	public void extents() {
//		assertThrows(UnsupportedOperationException.class, () -> frustum.extents());
//	}

	@Test
	public void intersectsExtents() {
		// TODO
	}

//	@Test
//	public void intersectsVolume() {
//		assertThrows(UnsupportedOperationException.class, () -> frustum.intersects(BoundingVolume.EMPTY));
//	}
//
//	@Test
//	public void intersectsRay() {
//		assertThrows(UnsupportedOperationException.class, () -> frustum.intersect(new Ray(Point.ORIGIN, Vector.X_AXIS)));
//	}
//
//	@Test
//	public void intersectsSphere() {
//		assertEquals(true, frustum.intersects(new Point(0, 0, 0), 1));
//		assertEquals(true, frustum.intersects(new Point(-1, 0, 0), 1));
//		assertEquals(true, frustum.intersects(new Point(-2, 0, 0), 1));
//		assertEquals(false, frustum.intersects(new Point(-3, 0, 0), 1));
//	}
}
