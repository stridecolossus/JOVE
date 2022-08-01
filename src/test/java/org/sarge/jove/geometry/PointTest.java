package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

@SuppressWarnings("static-method")
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
		final Point pt = new Point(4, 5, 6);
		assertEquals(27, pos.distance(pt));
		assertEquals(27, pt.distance(pos));
	}

	@Test
	void add() {
		assertEquals(new Point(5, 7, 9), pos.add(new Point(4, 5, 6)));
	}

	@Test
	void scale() {
		assertEquals(new Point(2, 4, 6), pos.scale(2));
	}

	@Test
	void layout() {
		assertEquals(3, Point.LAYOUT.size());
		assertEquals(Float.class, Point.LAYOUT.type());
		assertEquals(Float.BYTES, Point.LAYOUT.bytes());
	}

	@Test
	public void equals() {
		assertEquals(true, pos.equals(pos));
		assertEquals(true, pos.equals(new Point(1, 2, 3)));
		assertEquals(false, pos.equals(null));
		assertEquals(false, pos.equals(Point.ORIGIN));
		assertEquals(false, pos.equals(new Point(3, 4, 5)));
	}
}
