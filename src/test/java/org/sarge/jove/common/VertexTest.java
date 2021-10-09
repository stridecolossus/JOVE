package org.sarge.jove.common;

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
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Vertex.Builder;
import org.sarge.jove.common.Vertex.Component;
import org.sarge.jove.common.Vertex.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

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

	@Nested
	class LayoutTests {
		@Test
		void floats() {
			final Layout layout = Layout.of(2, Float.class);
			assertEquals(2, layout.size());
			assertEquals(Float.BYTES, layout.bytes());
			assertEquals(Float.class, layout.type());
			assertEquals(2 * 4, layout.length());
		}

		@Test
		void integers() {
			final Layout layout = Layout.of(2, Integer.class);
			assertEquals(2, layout.size());
			assertEquals(Integer.BYTES, layout.bytes());
			assertEquals(Integer.class, layout.type());
			assertEquals(2 * 4, layout.length());
		}

		@Test
		void shorts() {
			final Layout layout = Layout.of(2, Short.class);
			assertEquals(2, layout.size());
			assertEquals(Short.BYTES, layout.bytes());
			assertEquals(Short.class, layout.type());
			assertEquals(4, layout.length());
		}

		@Test
		void bytes() {
			final Layout layout = Layout.of(2, Byte.class);
			assertEquals(2, layout.size());
			assertEquals(Byte.BYTES, layout.bytes());
			assertEquals(Byte.class, layout.type());
			assertEquals(2, layout.length());
		}

		@Test
		void of() {
			final Layout layout = Layout.of(2, Float.class);
			assertEquals(layout, Layout.of(2));
		}

		@Test
		void stride() {
			final int stride = Layout.stride(List.of(Layout.of(2), Layout.of(3)));
			assertEquals((2 + 3) * Float.BYTES, stride);
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
		assertEquals(List.of(layout, layout), vertex.layout());
		assertEquals(2 * 3 * Float.BYTES, vertex.length());
		assertEquals(List.of(component, component).hashCode(), vertex.hashCode());
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
