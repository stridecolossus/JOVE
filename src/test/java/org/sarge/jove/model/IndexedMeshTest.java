package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.Point;

public class IndexedMeshTest {
	private IndexedMesh mesh;
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex(Point.ORIGIN);
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

	@DisplayName("An indexed mesh has a short index buffer if the index is small enough")
	@Test
	void shorts() {
		// Create index
		mesh.add(0);
		mesh.add(0);
		mesh.add(0);

		// Check index buffer
		final int len = 3 * Short.BYTES;
		final ByteSizedBufferable index = mesh.index().orElseThrow();
		final ByteBuffer bb = ByteBuffer.allocate(len);
		assertEquals(len, index.length());
		index.buffer(bb);
		assertEquals(0, bb.remaining());

		mesh.compact(false);
		assertEquals(3 * Integer.BYTES, index.length());
	}

	@DisplayName("An indexed mesh has an integer index buffer if the index is large")
	@Test
	void integers() {
		// Create an index larger than the size of a short value
		final int size = 65535;
		for(int n = 0; n < size; ++n) {
			mesh.add(0);
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
