package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.spy;
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
import org.sarge.jove.common.Layout;
import org.sarge.jove.common.Layout.Component;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.Builder;

public class VertexTest {
	@Nested
	class ComponentTests {
		@Test
		void length() {
			final Component component = spy(Component.class);
			when(component.layout()).thenReturn(Layout.of(2));
			assertEquals(2 * 4, component.length());
		}
	}

	private Vertex vertex;
	private Component component;
	private Layout layout;

	@BeforeEach
	void before() {
		layout = Layout.of(3);
		component = spy(Component.class);
		when(component.layout()).thenReturn(layout);
		vertex = Vertex.of(component, component);
	}

	@Test
	void constructor() {
		assertNotNull(vertex);
		assertEquals(List.of(component, component), vertex.components());
	}

	@Test
	void layout() {
		assertNotNull(vertex.layout());
		assertArrayEquals(new Layout[]{layout, layout}, vertex.layout().toArray());
	}

	@Test
	void length() {
		assertEquals(2 * 3 * Float.BYTES, vertex.length());
	}

	@Test
	void equals() {
		assertEquals(true, vertex.equals(vertex));
		assertEquals(true, vertex.equals(Vertex.of(component, component)));
		assertEquals(false, vertex.equals(null));
		assertEquals(false, vertex.equals(Vertex.of(component)));
	}

	@Test
	void get() {
		assertEquals(component, vertex.get(0));
		assertEquals(component, vertex.get(1));
	}

	@Test
	void buffer() {
		final int len = vertex.length();
		final ByteBuffer buffer = ByteBuffer.allocate(len);
		vertex.buffer(buffer);
		verify(component, times(2)).buffer(buffer);
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
			builder.position(Point.ORIGIN);
			builder.normal(Vector.X);
			builder.coordinate(Coordinate2D.BOTTOM_LEFT);
			builder.colour(Colour.WHITE);
			vertex = builder.build();
			assertNotNull(vertex);
			assertEquals(List.of(Point.ORIGIN, Vector.X, Coordinate2D.BOTTOM_LEFT, Colour.WHITE), vertex.components());
			assertEquals((3 + 3 + 2 + 4) * Float.BYTES, vertex.length());
		}
	}
}
