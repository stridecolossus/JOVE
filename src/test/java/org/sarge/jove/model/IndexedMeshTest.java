package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.*;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;

public class IndexedMeshTest {
	private IndexedMesh mesh;

	@BeforeEach
	void before() {
		mesh = new IndexedMesh(Primitive.TRIANGLE, List.of(Point.LAYOUT));
	}

	@Test
	void empty() {
		assertEquals(0, mesh.count());
		assertEquals(0, mesh.vertices().limit());
		assertEquals(0, mesh.index().get().limit());
		assertEquals(true, Primitive.TRIANGLE.isValidVertexCount(mesh.count()));
	}

	@Test
	void add() {
		mesh.add(new Vertex(Point.ORIGIN));
		mesh.add(0);
		mesh.add(0);
		mesh.add(0);
		assertEquals(3, mesh.count());
		assertEquals(3 * 4, mesh.vertices().limit());
		assertEquals(true, Primitive.TRIANGLE.isValidVertexCount(mesh.count()));

		final IntBuffer index = mesh.index().map(ByteBuffer::asIntBuffer).orElseThrow();
		assertEquals(3, index.limit());
		assertEquals(0, index.get());
		assertEquals(0, index.get());
		assertEquals(0, index.get());
	}

	@Test
	void invalid() {
		assertThrows(IndexOutOfBoundsException.class, () -> mesh.add(1));
	}

	@Nested
	class CompactIndexTest {
    	@Test
    	void compact() {
    		mesh.add(new Vertex(Point.ORIGIN));
    		mesh.add(0);
    		mesh.compact(true);
    		final var buffer = mesh.index().orElseThrow().asShortBuffer();
    		assertEquals(1, buffer.limit());
    		assertEquals((short) 0, buffer.get());
    	}

    	@Test
    	void restart() {
    		mesh.add(new Vertex(Point.ORIGIN));
    		mesh.restart();
    		mesh.compact(true);
    		final Exception e = assertThrows(IllegalStateException.class, () -> mesh.index());
    		assertEquals("Cannot restart a compact index", e.getMessage());
    	}

    	@Test
    	void invalid() {
    		mesh.add(new Vertex(Point.ORIGIN));
    		mesh.compact(true);
    		for(int n = 0; n < IndexedMesh.MAX_SHORT_INDEX_SIZE; ++n) {
    			mesh.add(0);
    		}
    		mesh.add(0);
    		final Exception e = assertThrows(IllegalStateException.class, () -> mesh.index());
    		assertEquals("Index is too large to be compact", e.getMessage());
    	}
	}
}
