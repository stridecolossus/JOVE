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
import org.sarge.jove.model.Vertex.Component.Type;
import org.sarge.jove.model.Vertex.MutableVertex;
import org.sarge.jove.texture.Image;
import org.sarge.jove.texture.TextureCoordinate.Coordinate2D;

public class VertexTest {
	@Nested
	class ComponentTests {
		@Test
		public void constructor() {
			final Component c = new Component(Type.INT, false, 2, 4);
			assertEquals(Type.INT, c.type());
			assertEquals(false, c.isSigned());
			assertEquals(2, c.size());
			assertEquals(4, c.bytes());
		}

		@Test
		public void position() {
			assertEquals(Type.FLOAT, Component.POSITION.type());
			assertEquals(true, Component.POSITION.isSigned());
			assertEquals(3, Component.POSITION.size());
			assertEquals(4, Component.POSITION.bytes());
		}

		@Test
		public void normal() {
			assertEquals(Type.FLOAT, Component.NORMAL.type());
			assertEquals(true, Component.NORMAL.isSigned());
			assertEquals(3, Component.NORMAL.size());
			assertEquals(4, Component.NORMAL.bytes());
		}

		@Test
		public void colour() {
			assertEquals(Type.FLOAT, Component.COLOUR.type());
			assertEquals(true, Component.COLOUR.isSigned());
			assertEquals(4, Component.COLOUR.size());
			assertEquals(4, Component.COLOUR.bytes());
		}

		@Test
		public void coordinates() {
			final Component tc = Component.coordinate(2);
			assertEquals(Type.FLOAT, tc.type());
			assertEquals(true, tc.isSigned());
			assertEquals(2, tc.size());
			assertEquals(4, tc.bytes());
		}

		@Test
		public void size() {
			assertEquals(3 + 4, Component.size(List.of(Component.POSITION, Component.COLOUR)));
		}

		@Test
		public void typeMapping() {
			assertEquals(Component.Type.NORM, Component.Type.of(Image.Type.BYTE));
			assertEquals(Component.Type.INT, Component.Type.of(Image.Type.INT));
			assertEquals(Component.Type.FLOAT, Component.Type.of(Image.Type.FLOAT));
		}
	}

	@Nested
	class MutableVertexTests {
		private MutableVertex vertex;

		@BeforeEach
		public void before() {
			vertex = new MutableVertex(Point.ORIGIN);
		}

		@Test
		public void constructor() {
			assertEquals(Point.ORIGIN, vertex.position());
			assertEquals(null, vertex.normal());
			assertEquals(null, vertex.coordinates());
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
			vertex.normal(Vector.X_AXIS);
			vertex.coordinates(Coordinate2D.TOP_RIGHT);
			vertex.buffer(buffer);

			final FloatBuffer expected = FloatBuffer.allocate(3 + 3 + 2);
			Point.ORIGIN.buffer(expected);
			Vector.X_AXIS.buffer(expected);
			Coordinate2D.TOP_RIGHT.buffer(expected);
			assertEquals(expected, buffer);
		}
	}
}
