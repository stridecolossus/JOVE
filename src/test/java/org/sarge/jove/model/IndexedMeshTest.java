package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.IndexedMesh.Index;

class IndexedMeshTest {
	private IndexedMesh mesh;

	@BeforeEach
	void before() {
		mesh = new IndexedMesh(Primitive.TRIANGLE, List.of(Point.LAYOUT));
	}

	@Test
	void empty() {
		assertEquals(0, mesh.count());
		assertEquals(true, mesh.index().isCompactIndex());
	}

	@Test
	void add() {
		mesh.add(new Vertex(Point.ORIGIN));
		mesh.add(0);
		mesh.add(0);
		assertEquals(2, mesh.count());
		assertEquals(true, mesh.index().isCompactIndex());
	}

	@Test
	void invalid() {
		assertThrows(IndexOutOfBoundsException.class, () -> mesh.add(1));
	}

	@Test
	void index() {
		// Build index
		mesh.add(new Vertex(Point.ORIGIN));
		mesh.add(new Vertex(Point.ORIGIN));
		mesh.add(new Vertex(Point.ORIGIN));
		mesh.add(0);
		mesh.add(1);
		mesh.add(2);
		assertEquals(3, mesh.count());

		// Write to buffer
		final Index index = mesh.index();
		final ByteBuffer buffer = ByteBuffer.allocate(3 * Integer.BYTES);
		assertEquals(index.length(), buffer.limit());
		index.buffer(buffer);
		assertEquals(0, buffer.remaining());
		assertEquals(true, index.isCompactIndex());

		// Check buffer
		final var check = buffer.rewind().asIntBuffer();
		assertEquals(0, check.get());
		assertEquals(1, check.get());
		assertEquals(2, check.get());
	}

	@Nested
	class CompactIndexTest {
		@Test
		void empty() {
			assertEquals(true, mesh.index().isCompactIndex());
		}

		@Test
		void index() {
			// Build index
			mesh.add(new Vertex(Point.ORIGIN));
			mesh.add(new Vertex(Point.ORIGIN));
			mesh.add(new Vertex(Point.ORIGIN));
			mesh.add(0);
			mesh.add(1);
			mesh.add(2);

			// Write to buffer
			final Index index = mesh.index().compact();
			final ByteBuffer buffer = ByteBuffer.allocate(3 * Short.BYTES);
			assertEquals(buffer.limit(), index.length());
			index.buffer(buffer);
			assertEquals(0, buffer.remaining());

			// Check buffer
			final var check = buffer.rewind().asShortBuffer();
			assertEquals(0, check.get());
			assertEquals(1, check.get());
			assertEquals(2, check.get());
		}

		@Test
		void restart() {
			mesh.restart();
			assertEquals(false, mesh.index().isCompactIndex());
			assertThrows(IllegalStateException.class, () -> mesh.index().compact());
		}

		@Test
		void maximum() {
			mesh.add(new Vertex(Point.ORIGIN));
			for(int n = 0; n < Index.MAX_SHORT_INDEX_SIZE; ++n) {
				mesh.add(0);
			}
			assertEquals(false, mesh.index().isCompactIndex());
			assertThrows(IllegalStateException.class, () -> mesh.index().compact());
		}
	}
}
