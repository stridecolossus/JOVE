package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BoundingBoxTest {
	private BoundingBox box;

	@BeforeEach
	public void before() {
		box = new BoundingBox(new Point(1, 2, 3), new Point(4, 5, 6));
	}

	@Test
	public void contains() {
		assertTrue(box.contains(new Point(1, 2, 3)));
		assertTrue(box.contains(new Point(2, 3, 4)));
		assertTrue(box.contains(new Point(4, 5, 6)));
		assertFalse(box.contains(Point.ORIGIN));
	}

	@Test
	public void intersect() {
		// TODO
	}

	@Test
	public void extents() {
		assertEquals(new Extents(new Point(1, 2, 3), new Point(4, 5, 6)), box.extents());
	}
}
