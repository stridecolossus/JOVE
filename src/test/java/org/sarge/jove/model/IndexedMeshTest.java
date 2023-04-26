package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.Point;

public class IndexedMeshTest {
	private IndexedMeshBuilder builder;
	private Mesh mesh;
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex(Point.ORIGIN);
		builder = new IndexedMeshBuilder(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT));
		builder.add(vertex);
		mesh = builder.mesh();
	}

	@Test
	void constructor() {
		assertEquals(0, mesh.count());
	}

	@DisplayName("Vertex indices can be added to an indexed mesh")
	@Test
	void add() {
		builder.add(0);
		assertEquals(1, mesh.count());
	}

	@DisplayName("An invalid vertex index cannot be added to the mesh")
	@Test
	void invalid() {
		assertThrows(IndexOutOfBoundsException.class, () -> builder.add(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> builder.add(1));
	}

	@DisplayName("The index can be restarted")
	@Test
	void restart() {
		builder.add(0);
		builder.restart();
		assertEquals(1, mesh.count());
	}

	@DisplayName("An indexed mesh has a short index buffer if the index is small enough")
	@Test
	void shorts() {
		// Create index
		builder.add(0);
		builder.add(0);
		builder.add(0);

		// Check index buffer
		final int len = 3 * Short.BYTES;
		final ByteSizedBufferable index = mesh.index().orElseThrow();
		final ByteBuffer bb = ByteBuffer.allocate(len);
		assertEquals(len, index.length());
		index.buffer(bb);
		assertEquals(0, bb.remaining());

		builder.compact(false);
		assertEquals(3 * Integer.BYTES, index.length());
	}

	@DisplayName("An indexed mesh has an integer index buffer if the index is large")
	@Test
	void integers() {
		// Create an index larger than the size of a short value
		final int size = 65535;
		for(int n = 0; n < size; ++n) {
			builder.add(0);
		}

		// Check index is integral
		final int len = size * Integer.BYTES;
		final ByteSizedBufferable index = mesh.index().orElseThrow();
		assertEquals(len, index.length());

		// Check index buffer
		final ByteBuffer bb = ByteBuffer.allocate(len);
		index.buffer(bb);
		// TODO - non-direct buffer does not get updated!!!
		// assertEquals(0, bb.remaining());
	}
}
