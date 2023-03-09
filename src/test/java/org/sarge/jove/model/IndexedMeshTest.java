package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;

public class IndexedMeshTest {
	private IndexedMesh mesh;
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new SimpleVertex(Point.ORIGIN);
		mesh = new IndexedMesh(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT));
		mesh.add(vertex);
	}

	@Test
	void constructor() {
		assertEquals(true, mesh.isIndexed());
		assertEquals(0, mesh.count());
	}

	@DisplayName("Vertex indices can be added to an indexed mesh")
	@Test
	void add() {
		mesh.add(0);
		assertEquals(1, mesh.count());
		assertArrayEquals(new int[]{0}, mesh.index().toArray());
	}

	@DisplayName("An invalid vertex index cannot be added to the mesh")
	@Test
	void invalid() {
		assertThrows(IndexOutOfBoundsException.class, () -> mesh.add(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> mesh.add(1));
	}

	@DisplayName("The index can be restarted")
	@Test
	void restart() {
		mesh.add(0);
		mesh.restart();
		assertEquals(1, mesh.count());
	}

	@DisplayName("The triangles of an indexed mesh can be enumerated")
	@Test
	void triangles() {
		final Point a = new Point(3, 0, 0);
		final Point b = new Point(3, 3, 0);
		mesh.add(new SimpleVertex(a));
		mesh.add(new SimpleVertex(b));
		mesh.add(0);
		mesh.add(1);
		mesh.add(2);
		assertArrayEquals(new int[][]{new int[]{0, 1, 2}}, mesh.triangles().toArray());
	}

	@DisplayName("The triangles of an indexed mesh ignore index restarts")
	@Test
	void skip() {
		mesh.add(0);
		mesh.add(0);
		mesh.add(0);
		mesh.restart();
		mesh.add(0);
		mesh.add(0);
		mesh.add(0);
		assertEquals(2, mesh.triangles().count());
	}

	@DisplayName("Vertex normals can be computed for an indexed mesh")
	@Test
	void compute() {
		final var vertex = new MutableVertex();
		vertex.position(Point.ORIGIN);
		vertex.normal(Axis.Y);
		mesh = new IndexedMesh(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT, Normal.LAYOUT));
		mesh.add(vertex);
		mesh.add(0);
		mesh.add(0);
		mesh.add(0);
		mesh.compute();
	}

	@DisplayName("A indexed mesh that has been buffered...")
	@Nested
	class BufferedModelTests {
		private BufferedMesh buffer;

		@BeforeEach
		void before() {
			buffer = mesh.buffer();
		}

		@Test
		void constructor() {
			assertEquals(mesh, buffer);
			assertEquals(true, buffer.isIndexed());
		}

		@DisplayName("has a short index buffer if the index is small enough")
		@Test
		void shorts() {
			// Create index
			mesh.add(0);
			mesh.add(0);
			mesh.add(0);

			// Check index buffer
			final int len = 3 * Short.BYTES;
			final ByteSizedBufferable index = buffer.indexBuffer().orElseThrow();
			final ByteBuffer bb = ByteBuffer.allocate(len);
			assertEquals(len, index.length());
			index.buffer(bb);
			assertEquals(0, bb.remaining());

			mesh.compact(false);
			assertEquals(3 * Integer.BYTES, index.length());
		}

		@DisplayName("has an integer index buffer if the index is large")
		@Test
		void integers() {
			// Create an index larger than the size of a short value
			final int size = 65535;
			for(int n = 0; n < size; ++n) {
				mesh.add(0);
			}

			// Check index is integral
			final int len = size * Integer.BYTES;
			final ByteSizedBufferable index = buffer.indexBuffer().orElseThrow();
			assertEquals(len, index.length());

			// Check index buffer
			final ByteBuffer bb = ByteBuffer.allocate(len);
			index.buffer(bb);
			// TODO - non-direct buffer does not get updated!!!
			// assertEquals(0, bb.remaining());
		}
	}
}
