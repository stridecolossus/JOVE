package org.sarge.jove.geometry;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SphereVolumeTest {
	private SphereVolume sphere;

	@BeforeEach
	public void before() {
		sphere = new SphereVolume(Point.ORIGIN, 5);
	}

	@Test
	public void constructor() {
		assertEquals(Point.ORIGIN, sphere.centre());
		assertEquals(5f, sphere.radius(), 0.0001f);
	}

	@Test
	public void extentsConstructor() {
		sphere = SphereVolume.of(new Extents(new Point(1, 2, 3), new Point(5, 6, 7)));
		assertNotNull(sphere);
		assertEquals(new Point(3, 4, 5), sphere.centre());
		assertEquals(2f, sphere.radius(), 0.0001f);
	}

	@Test
	public void contains() {
		assertEquals(true, sphere.contains(Point.ORIGIN));
		assertEquals(true, sphere.contains(new Point(5, 0, 0)));
		assertEquals(false, sphere.contains(new Point(6, 0, 0)));
	}

	@Test
	public void intersectsSelf() {
		assertEquals(true, sphere.intersects(sphere));
	}

	@Test
	public void intersectsSphere() {
		assertEquals(true, sphere.intersects(new SphereVolume(new Point(3, 0, 0), 1)));
		assertEquals(false, sphere.intersects(new SphereVolume(new Point(6, 0, 0), 1)));
	}

	@Test
	public void intersectsExtents() {
		// TODO
	}

	@Test
	public void extents() {
		assertEquals(new Extents(new Point(-5, -5, -5), new Point(5, 5, 5)), sphere.extents());
	}
}
