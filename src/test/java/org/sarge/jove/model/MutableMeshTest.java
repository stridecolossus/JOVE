package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.scene.volume.Bounds;

class MutableMeshTest {
	private MutableMesh mesh;
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex(Point.ORIGIN, Axis.Z, Coordinate2D.TOP_LEFT);
		mesh = new MutableMesh(Primitive.TRIANGLE, Point.LAYOUT, Normal.LAYOUT, Coordinate2D.LAYOUT);
	}

	@Nested
	class Empty {
		@Test
		void count() {
			assertEquals(0, mesh.count());
		}

		@Test
		void vertices() {
			final var vertices = mesh.vertices();
			assertEquals(0, vertices.length());
		}

		@Test
		void bounds() {
			assertEquals(Bounds.EMPTY, mesh.bounds());
		}
	}

	@Nested
	class Added {
		@BeforeEach
		void before() {
			mesh.add(vertex);
		}

		@Test
		void count() {
			assertEquals(1, mesh.count());
		}

		@Test
		void vertices() {
			final var vertices = mesh.vertices();
			final int length = (3 + 3 + 2) * 4;
			assertEquals(length, vertices.length());

			final var buffer = ByteBuffer.allocate(length);
			vertices.buffer(buffer);
			assertEquals(0, buffer.remaining());
		}

		@Test
		void bounds() {
			final Point p = new Point(1, 2, 3);
			mesh.add(vertex);
			mesh.add(new Vertex(p, Axis.Z, Coordinate2D.TOP_LEFT));
			assertEquals(new Bounds(Point.ORIGIN, p), mesh.bounds());
		}
	}

	@Nested
	class RemoveLayoutTest {
		@BeforeEach
		void before() {
			mesh.add(vertex);
		}

		@Test
		void remove() {
			mesh.remove(Normal.LAYOUT);
			assertEquals(List.of(Point.LAYOUT, Coordinate2D.LAYOUT), mesh.layout());
			assertEquals(List.of(Point.ORIGIN, Coordinate2D.TOP_LEFT), vertex.components());
		}

		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> mesh.remove(Colour.LAYOUT));
		}
	}

	@Test
	void compute() {
		mesh.add(new Vertex(Point.ORIGIN, Axis.X));
		mesh.add(new Vertex(new Point(1, 0, 0), Axis.X));
		mesh.add(new Vertex(new Point(1, 1, 0), Axis.X));
		mesh.compute();
		// TODO - Axis.Z
	}
}
