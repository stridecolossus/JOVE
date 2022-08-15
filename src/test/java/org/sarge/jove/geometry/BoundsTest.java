package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.*;

import org.junit.jupiter.api.*;

class BoundsTest {
	private Bounds bounds;
	private Point min, max;
	private Point centre;

	@BeforeEach
	void before() {
		min = new Point(1, 2, 3);
		max = new Point(5, 6, 7);
		bounds = new Bounds(min, max);
		centre = new Point(3, 4, 5);
	}

	@Test
	void constructor() {
		assertEquals(min, bounds.min());
		assertEquals(max, bounds.max());
	}

	@Test
	void centre() {
		assertEquals(centre, bounds.centre());
	}

	@Test
	void contains() {
		assertEquals(true, bounds.contains(min));
		assertEquals(true, bounds.contains(max));
		assertEquals(true, bounds.contains(centre));
		assertEquals(false, bounds.contains(Point.ORIGIN));
	}

	@Test
	void largest() {
		assertEquals(4, bounds.largest());
	}

	@Test
	void nearest() {
		assertEquals(min, bounds.nearest(min));
		assertEquals(max, bounds.nearest(max));
		assertEquals(min, bounds.nearest(Point.ORIGIN));
		assertEquals(new Point(5, 2, 3), bounds.nearest(new Point(999, 0, 0)));
	}

	@Test
	void positive() {
		assertEquals(max, bounds.positive(Vector.X));
		assertEquals(max, bounds.positive(Vector.Y));
		assertEquals(max, bounds.positive(Vector.Z));
		assertEquals(new Point(1, 6, 7), bounds.positive(Vector.X.invert()));
		assertEquals(new Point(5, 2, 7), bounds.positive(Vector.Y.invert()));
		assertEquals(new Point(5, 6, 3), bounds.positive(Vector.Z.invert()));

	}

	@Test
	void negative() {
		assertEquals(new Point(1, 6, 7), bounds.negative(Vector.X));
		assertEquals(new Point(5, 2, 7), bounds.negative(Vector.Y));
		assertEquals(new Point(5, 6, 3), bounds.negative(Vector.Z));
		assertEquals(max, bounds.negative(Vector.X.invert()));
		assertEquals(max, bounds.negative(Vector.Y.invert()));
		assertEquals(max, bounds.negative(Vector.Z.invert()));
	}

	@Test
	void equals() {
		assertEquals(true, bounds.equals(bounds));
		assertEquals(true, bounds.equals(new Bounds(min, max)));
		assertEquals(false, bounds.equals(null));
		assertEquals(false, bounds.equals(new Bounds(max, min)));
	}

	@Nested
	class IntersectionTests {
		@Test
		void self() {
			assertEquals(true, bounds.intersects(bounds));
		}

		@Test
		void touching() {
			assertEquals(true, bounds.intersects(new Bounds(Point.ORIGIN, min)));
			assertEquals(true, bounds.intersects(new Bounds(min, centre)));
			assertEquals(true, bounds.intersects(new Bounds(centre, max)));
		}

		@Test
		void inside() {
			assertEquals(true, bounds.intersects(new Bounds(centre, centre)));
		}

		@Test
		void outside() {
			assertEquals(false, bounds.intersects(new Bounds(Point.ORIGIN, Point.ORIGIN)));
		}
	}

	@Nested
	class BuilderTests {
		@Test
		void build() {
			final Bounds result = new Bounds.Builder().add(min).add(max).build();
			assertEquals(bounds, result);
		}

		@Test
		void buildEmpty() {
			bounds = new Bounds.Builder().build();
			assertNotNull(bounds);
			assertEquals(Float.MAX_VALUE, Math.abs(bounds.largest()));
		}
	}

	@Nested
	class CollectorTests {
		private Collector<Point, ?, Bounds> collector;

		@BeforeEach
		void before() {
			collector = Bounds.collector();
		}

		@Test
		void constructor() {
			assertNotNull(collector);
		}

		@Test
		void collect() {
			assertEquals(bounds, Stream.of(min, max).collect(Bounds.collector()));
		}

		@Test
		void combiner() {
			final Bounds.Builder left = new Bounds.Builder().add(min);
			final Bounds.Builder right = new Bounds.Builder().add(max);
			final Bounds.Builder combined = Bounds.Builder.combine(left, right);
			assertNotNull(combined);
			assertEquals(bounds, combined.build());
		}
	}
}
