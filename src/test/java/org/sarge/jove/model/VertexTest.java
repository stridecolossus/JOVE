package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

public class VertexTest {
	@Nested
	class BuilderTests {
		private Vertex.Builder builder;

		@BeforeEach
		void before() {
			builder = new Vertex.Builder();
		}

		@Test
		void position() {
			builder.add(Point.ORIGIN);
			assertEquals(new SimpleVertex(Point.ORIGIN), builder.build());
		}

		@Test
		void coordinate() {
			builder.add(Point.ORIGIN);
			builder.add(Coordinate2D.BOTTOM_LEFT);
			assertEquals(new DefaultVertex(Point.ORIGIN, Coordinate2D.BOTTOM_LEFT), builder.build());
		}

		@Test
		void all() {
			builder.add(Point.ORIGIN);
			builder.add(Axis.Y);
			builder.add(Coordinate2D.BOTTOM_LEFT);
			final var expected = new MutableVertex();
			expected.position(Point.ORIGIN);
			expected.normal(Axis.Y);
			expected.coordinate(Coordinate2D.BOTTOM_LEFT);
			assertEquals(expected, builder.build());
		}

		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
