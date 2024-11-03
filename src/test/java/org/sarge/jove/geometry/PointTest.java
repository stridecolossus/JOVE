package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;

class PointTest {
	private Point pos;

	@BeforeEach
	void before() {
		pos = new Point(1, 2, 3);
	}

	@Test
	void constructor() {
		assertEquals(1, pos.x);
		assertEquals(2, pos.y);
		assertEquals(3, pos.z);
	}

	@Test
	void copy() {
		assertEquals(pos, new Point(pos));
	}

	@Test
	void array() {
		assertEquals(pos, new Point(new float[]{1, 2, 3}));
	}

	@Test
	void origin() {
		assertEquals(new Point(0, 0, 0), Point.ORIGIN);
	}

	@Test
	void distance() {
		final Point p = new Point(4, 5, 6);
		assertEquals(27, pos.distance(p));
		assertEquals(27, p.distance(pos));
	}

	@Test
	void add() {
		assertEquals(new Point(5, 7, 9), pos.add(new Vector(4, 5, 6)));
	}

	@Test
	void layout() {
		assertEquals(Layout.floats(3), Point.LAYOUT);
		assertEquals(Point.LAYOUT, pos.layout());
	}

	@Test
	public void equals() {
		assertEquals(pos, pos);
		assertEquals(pos, new Point(1, 2, 3));
		assertNotEquals(pos, null);
		assertNotEquals(pos, Point.ORIGIN);
	}
}
