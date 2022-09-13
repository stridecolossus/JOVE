package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Component;

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
	void subtract() {
		assertEquals(Point.ORIGIN, pos.subtract(pos));
	}

	@Test
	void multiply() {
		assertEquals(new Point(2, 4, 6), pos.multiply(2));
	}

	@Test
	void layout() {
		assertEquals(3, Point.LAYOUT.size());
		assertEquals(Component.Type.FLOAT, Point.LAYOUT.type());
		assertEquals(Float.BYTES, Point.LAYOUT.bytes());
	}

	@Test
	public void equals() {
		assertEquals(pos, pos);
		assertEquals(pos, new Point(1, 2, 3));
		assertNotEquals(pos, null);
		assertNotEquals(pos, Point.ORIGIN);
	}
}
