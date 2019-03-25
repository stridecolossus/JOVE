package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.FloatBuffer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.jove.model.Vertex.MutableVertex;
import org.sarge.jove.texture.TextureCoordinate.Coordinate2D;

public class VertexTest {
	@Nested
	class ComponentTests {
		@Test
		public void constructor() {
			final Component c = new Component(3);
			assertEquals(3, c.size());
		}

		@Test
		public void position() {
			assertEquals(3, Component.POSITION.size());
		}

		@Test
		public void normal() {
			assertEquals(3, Component.NORMAL.size());
		}

		@Test
		public void colour() {
			assertEquals(4, Component.COLOUR.size());
		}

		@Test
		public void coordinates() {
			assertEquals(1, Component.coordinate(1).size());
			assertEquals(2, Component.coordinate(2).size());
			assertEquals(3, Component.coordinate(3).size());
		}

		@Test
		public void size() {
			assertEquals(3 + 4, Component.size(List.of(Component.NORMAL, Component.COLOUR)));
		}
	}

	@Nested
	class MutableVertexTests {
		private MutableVertex vertex;

		@BeforeEach
		public void before() {
			vertex = new MutableVertex();
		}

		@Test
		public void constructor() {
			assertEquals(Point.ORIGIN, vertex.position());
			assertEquals(new Vector(0, 0, 0), vertex.normal());
			assertEquals(Coordinate2D.Corner.BOTTOM_LEFT.coordinates(), vertex.coordinates());
		}

		@Test
		public void position() {
			final Point pos = new Point(1, 2, 3);
			vertex.position(pos);
			assertEquals(pos, vertex.position());
		}

		@Test
		public void normal() {
			vertex.normal(Vector.X_AXIS);
			assertEquals(Vector.X_AXIS, vertex.normal());
		}

		@Test
		public void coords() {
			final Coordinate2D coords = new Coordinate2D(1, 1);
			vertex.coordinates(coords);
			assertEquals(coords, vertex.coordinates());
		}

		@Test
		public void buffer() {
			final FloatBuffer buffer = FloatBuffer.allocate(3 + 3 + 2);
			final FloatBuffer expected = FloatBuffer.allocate(3 + 3 + 2);
			vertex.buffer(buffer);
			buffer.flip();
			assertEquals(expected, buffer);
		}
	}
}
