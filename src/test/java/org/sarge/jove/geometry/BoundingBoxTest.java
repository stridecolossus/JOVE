package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class BoundingBoxTest {
	private BoundingBox box;

	@Before
	public void before() {
		box = new BoundingBox(new Point(1, 2, 3), new Point(4, 5, 6));
	}

	@Test
	public void constructor() {
		assertEquals(new Point(1, 2, 3), box.getMinimum());
		assertEquals(new Point(4, 5, 6), box.getMaximum());
		assertEquals(new Point(2.5f, 3.5f, 4.5f), box.getCentre());
	}

	@Test
	public void contains() {
		assertTrue(box.contains(new Point(1, 2, 3)));
		assertTrue(box.contains(new Point(3, 4, 5)));
		assertFalse(box.contains(new Point(0, 0, 0)));
	}

	@Test
	public void pointsConstructor() {
		final Point[] points = {
				new Point(1, 0, 0),
				new Point(0, 2, 0),
				new Point(0, 0, 3),
		};
		box = BoundingBox.of(Arrays.asList(points).stream());
		assertEquals(new Point(0, 0, 0), box.getMinimum());
		assertEquals(new Point(1, 2, 3), box.getMaximum());
	}
}
