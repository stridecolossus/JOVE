package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.stream.Collector;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Extents.Builder;

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
	void positive() {
		assertEquals(max, extents.positive(Vector.X_AXIS));
		assertEquals(max, extents.positive(Vector.Y_AXIS));
		assertEquals(max, extents.positive(Vector.Z_AXIS));
		assertEquals(new Point(1, 6, 7), extents.positive(Vector.X_AXIS.negate()));
		assertEquals(new Point(5, 2, 7), extents.positive(Vector.Y_AXIS.negate()));
		assertEquals(new Point(5, 6, 3), extents.positive(Vector.Z_AXIS.negate()));

	}

	@Test
	void negative() {
		assertEquals(new Point(1, 6, 7), extents.negative(Vector.X_AXIS));
		assertEquals(new Point(5, 2, 7), extents.negative(Vector.Y_AXIS));
		assertEquals(new Point(5, 6, 3), extents.negative(Vector.Z_AXIS));
		assertEquals(max, extents.negative(Vector.X_AXIS.negate()));
		assertEquals(max, extents.negative(Vector.Y_AXIS.negate()));
		assertEquals(max, extents.negative(Vector.Z_AXIS.negate()));
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
	void equals() {
		assertEquals(true, extents.equals(extents));
		assertEquals(true, extents.equals(new Extents(min, max)));
		assertEquals(false, extents.equals(null));
		assertEquals(false, extents.equals(mock(Extents.class)));
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

	@Nested
	class BuilderTests {
		@Test
		void build() {
			final Extents result = new Builder().add(min).add(max).build();
			assertEquals(extents, result);
		}

		@Test
		void buildEmpty() {
			extents = new Builder().build();
			assertNotNull(extents);
			assertEquals(Float.MAX_VALUE, Math.abs(extents.largest()));
		}
	}

	@Nested
	class CollectorTests {
		private Collector<Point, ?, Extents> collector;

		@BeforeEach
		void before() {
			collector = Extents.collector();
		}

		@Test
		void constructor() {
			assertNotNull(collector);
		}

		@Test
		void collect() {
			assertEquals(extents, Stream.of(min, max).collect(Extents.collector()));
		}

		@Test
		void combiner() {
			final Builder left = new Builder().add(min);
			final Builder right = new Builder().add(max);
			final Builder combined = Builder.combine(left, right);
			assertNotNull(combined);
			assertEquals(extents, combined.build());
		}
	}
}
