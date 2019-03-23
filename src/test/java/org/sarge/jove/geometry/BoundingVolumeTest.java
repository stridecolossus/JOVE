package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class BoundingVolumeTest {
	@Test
	public void empty() {
		assertEquals(false, BoundingVolume.EMPTY.contains(Point.ORIGIN));
		assertEquals(Optional.empty(), BoundingVolume.EMPTY.intersect(null));
		assertNotNull(BoundingVolume.EMPTY.extents());
	}

	@Test
	public void inverse() {
		final BoundingVolume sphere = new SphereVolume(Point.ORIGIN, 3);
		final BoundingVolume inverse = BoundingVolume.inverse(sphere);
		assertEquals(false, inverse.contains(Point.ORIGIN));
		assertEquals(false, inverse.contains(new Point(3, 0, 0)));
		// TODO
		//assertEquals(false, BoundingVolume.EMPTY.intersect(null));
		//assertNotNull(BoundingVolume.EMPTY.extents());
	}
}
