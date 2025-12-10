package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Mesh.Index;

class IndexedMeshTest {
	private IndexedMesh mesh;
	private Index index;
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex(Point.ORIGIN);
		mesh = new IndexedMesh(Primitive.TRIANGLE, Point.LAYOUT);
		index = mesh.index().orElseThrow();
	}

	@Test
	void empty() {
		assertEquals(0, mesh.count());
		assertEquals(0, index.length());
	}

	@Test
	void add() {
		mesh.add(vertex);
		mesh.add(0);
		mesh.add(0);
		assertEquals(2, mesh.count());
	}

	@Test
	void invalid() {
		assertThrows(IndexOutOfBoundsException.class, () -> mesh.add(1));
	}

	@Test
	void index() {
		// Build index
		mesh.add(vertex);
		mesh.add(vertex);
		mesh.add(vertex);
		mesh.add(0);
		mesh.add(1);
		mesh.add(2);
		assertEquals(3, mesh.count());

		// Write to buffer
		final Index index = mesh.index().orElseThrow();
		final ByteBuffer buffer = ByteBuffer.allocate(3 * Integer.BYTES);
		assertEquals(index.length(), buffer.limit());
		index.buffer(buffer);
		assertEquals(0, buffer.remaining());

		// Check buffer
		final var check = buffer.rewind().asIntBuffer();
		assertEquals(0, check.get());
		assertEquals(1, check.get());
		assertEquals(2, check.get());
	}

	@DisplayName("A mesh with less than 256 vertices can be indexed by 8-bit indices")
	@Test
	void byteIndex() {
		for(int n = 0; n < 255; ++n) {
			mesh.add(vertex);
		}
		assertEquals(1, index.minimumElementBytes());
		assertNotNull(index.index(1));
	}

	@DisplayName("A mesh with less than 64K vertices can be indexed by 16-bit values")
	@Test
	void shortIndex() {
		for(int n = 0; n < 65535; ++n) {
			mesh.add(vertex);
		}
		assertEquals(2, index.minimumElementBytes());
		assertNotNull(index.index(2));
		assertThrows(IllegalArgumentException.class, () -> index.index(1));
	}

	@DisplayName("A mesh with 64K or more vertices can only be indexed by 32-bit values")
	@Test
	void integerIndex() {
		for(int n = 0; n < 65536; ++n) {
			mesh.add(vertex);
		}
		assertEquals(4, index.minimumElementBytes());
		assertNotNull(index.index(4));
		assertThrows(IllegalArgumentException.class, () -> index.index(1));
		assertThrows(IllegalArgumentException.class, () -> index.index(2));
	}

	@DisplayName("A mesh containing index restarts can only be indexed by 32-bit values")
	@Test
	void restartIndex() {
		mesh.restart();
		assertEquals(4, index.minimumElementBytes());
		assertNotNull(index.index(4));
		assertThrows(IllegalArgumentException.class, () -> index.index(1));
		assertThrows(IllegalArgumentException.class, () -> index.index(2));
	}
}
