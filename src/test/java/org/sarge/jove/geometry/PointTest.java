package org.sarge.jove.geometry;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.util.MathsUtil;

public class PointTest {
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
	void origin() {
		assertEquals(new Point(0, 0, 0), Point.ORIGIN);
	}

	@Test
	void distance() {
		assertTrue(MathsUtil.isEqual(27, pos.distance(new Point(4, 5, 6))));
	}

	@Test
	void scale() {
		assertEquals(new Point(2, 4, 6), pos.scale(2));
	}
}
