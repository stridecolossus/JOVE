package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

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
		assertEquals(max, bounds.positive(Axis.X.vector()));
		assertEquals(max, bounds.positive(Axis.Y.vector()));
		assertEquals(max, bounds.positive(Axis.Z.vector()));
		assertEquals(new Point(1, 6, 7), bounds.positive(Axis.X.vector().invert()));
		assertEquals(new Point(5, 2, 7), bounds.positive(Axis.Y.vector().invert()));
		assertEquals(new Point(5, 6, 3), bounds.positive(Axis.Z.vector().invert()));

	}

	@Test
	void negative() {
		assertEquals(new Point(1, 6, 7), bounds.negative(Axis.X.vector()));
		assertEquals(new Point(5, 2, 7), bounds.negative(Axis.Y.vector()));
		assertEquals(new Point(5, 6, 3), bounds.negative(Axis.Z.vector()));
		assertEquals(max, bounds.negative(Axis.X.vector().invert()));
		assertEquals(max, bounds.negative(Axis.Y.vector().invert()));
		assertEquals(max, bounds.negative(Axis.Z.vector().invert()));
	}

	@Test
	void equals() {
		assertEquals(true, bounds.equals(bounds));
		assertEquals(true, bounds.equals(new Bounds(min, max)));
		assertEquals(false, bounds.equals(null));
		assertEquals(false, bounds.equals(new Bounds(max, min)));
	}

	@Test
	void empty() {
		assertEquals(Point.ORIGIN, Bounds.EMPTY.min());
		assertEquals(Point.ORIGIN, Bounds.EMPTY.max());
		assertEquals(Point.ORIGIN, Bounds.EMPTY.centre());
		assertEquals(0, Bounds.EMPTY.largest());
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

	@DisplayName("A bounds can be created as an aggregate of a collection of bounds")
	@Test
	void sum() {
		assertEquals(bounds, Bounds.sum(List.of(bounds, bounds)));
	}

	@DisplayName("A bounds builder...")
	@Nested
	class BuilderTests {
		private Bounds.Builder builder;

		@BeforeEach
		void before() {
			builder = new Bounds.Builder();
		}

		@DisplayName("creates an infinite bounds by default")
		@Test
		void infinite() {
			final Bounds result = builder.build();
			assertEquals(new Point(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE), result.min());
			assertEquals(new Point(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE), result.max());
		}

		@DisplayName("can add points to the bounds")
		@Test
		void add() {
			builder.add(min).add(max);
			assertEquals(bounds, builder.build());
		}

		@DisplayName("can construct an empty bounds about the origin")
		@Test
		void empty() {
			builder.add(Point.ORIGIN);
			assertEquals(Bounds.EMPTY, builder.build());
		}

		@DisplayName("can add other bounds")
		@Test
		void bounds() {
			builder.add(bounds);
			assertEquals(bounds, builder.build());
		}
	}
}
