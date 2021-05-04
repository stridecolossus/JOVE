package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.Builder;

class VertexTest {
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = Vertex.of(Point.ORIGIN, Colour.BLACK);
	}

	@Test
	void components() {
		assertEquals(List.of(Point.ORIGIN, Colour.BLACK), vertex.components());
	}

	@Test
	void get() {
		assertEquals(Point.ORIGIN, vertex.get(0));
		assertEquals(Colour.BLACK, vertex.get(1));
	}

	@Test
	void layout() {
		assertEquals(List.of(Point.ORIGIN.layout(), Colour.BLACK.layout()), vertex.layout());
	}

	@Test
	void length() {
		assertEquals((3 + 4) * Float.BYTES, vertex.length());
	}

	@Test
	void buffer() {
		final ByteBuffer buffer = mock(ByteBuffer.class);
		when(buffer.putFloat(anyFloat())).thenReturn(buffer);
		vertex.buffer(buffer);
		verify(buffer, times(3 + 3)).putFloat(0);
		verify(buffer, times(1)).putFloat(1);
	}

	@Test
	void equals() {
		assertEquals(true, vertex.equals(vertex));
		assertEquals(true, vertex.equals(Vertex.of(Point.ORIGIN, Colour.BLACK)));
		assertEquals(false, vertex.equals(null));
		assertEquals(false, vertex.equals(Vertex.of(Point.ORIGIN)));
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder();
		}

		@Test
		void build() {
			// Build a vertex
			vertex = builder
					.position(Point.ORIGIN)
					.normal(Vector.X)
					.coordinate(Coordinate2D.BOTTOM_LEFT)
					.colour(Colour.BLACK)
					.build();

			// Validate vertex
			assertNotNull(vertex);
			assertNotNull(vertex.layout());
			assertEquals(4, vertex.layout().size());
			assertEquals((3 + 3 + 2 + 4) * Float.BYTES, vertex.length());
		}
	}
}
