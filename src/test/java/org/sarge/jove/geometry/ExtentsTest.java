package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExtentsTest {
	private Extents extents;
	private Point min, max;
	private Point centre;

	@BeforeEach
	void before() {
		min = new Point(1, 2, 3);
		max = new Point(5, 6, 7);
		extents = new Extents(min, max);
		centre = new Point(3, 4, 5);
	}

	@Test
	void constructor() {
		assertEquals(min, extents.min());
		assertEquals(max, extents.max());
	}

	@Test
	void centre() {
		assertEquals(centre, extents.centre());
	}

	@Test
	void contains() {
		assertEquals(true, extents.contains(min));
		assertEquals(true, extents.contains(max));
		assertEquals(true, extents.contains(centre));
		assertEquals(false, extents.contains(Point.ORIGIN));
	}

	@Nested
	class IntersectionTests {
		@Test
		void self() {
			assertEquals(true, extents.intersects(extents));
		}

		@Test
		void touching() {
			assertEquals(true, extents.intersects(new Extents(Point.ORIGIN, min)));
			assertEquals(true, extents.intersects(new Extents(min, centre)));
			assertEquals(true, extents.intersects(new Extents(centre, max)));
		}

		@Test
		void inside() {
			assertEquals(true, extents.intersects(new Extents(centre, centre)));
		}

		@Test
		void outside() {
			assertEquals(false, extents.intersects(new Extents(Point.ORIGIN, Point.ORIGIN)));
		}
	}

	@Test
	void largest() {
		assertEquals(4, extents.largest());
	}

	@Test
	void nearest() {
		assertEquals(min, extents.nearest(min));
		assertEquals(max, extents.nearest(max));
		assertEquals(min, extents.nearest(Point.ORIGIN));
		assertEquals(new Point(5, 2, 3), extents.nearest(new Point(999, 0, 0)));
	}

	@Test
	void invert() {
		final Extents inverse = extents.invert();
		assertNotNull(inverse);
		assertEquals(min, inverse.min());
		assertEquals(max, inverse.max());
		assertEquals(centre, inverse.centre());
		assertEquals(false, inverse.contains(min));
		assertEquals(false, inverse.contains(max));
		assertEquals(false, inverse.contains(centre));
		assertEquals(true, inverse.contains(Point.ORIGIN));
	}

	@Test
	void build() {
		final Extents result = new Extents.Builder().add(min).add(max).build();
		assertEquals(extents, result);
	}

	@Test
	void buildEmpty() {
		extents = new Extents.Builder().build();
		assertNotNull(extents);
		assertEquals(Float.MAX_VALUE, Math.abs(extents.largest()));
	}

	@Test
	void equals() {
		assertEquals(true, extents.equals(extents));
		assertEquals(true, extents.equals(new Extents(min, max)));
		assertEquals(false, extents.equals(null));
		assertEquals(false, extents.equals(mock(Extents.class)));
	}
}
