package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.util.IntegerList;

class MutableModelTest {
	private MutableModel model;
	private Vertex vertex;
	private IntegerList index;

	@BeforeEach
	void before() {
		vertex = new Vertex().position(Point.ORIGIN).colour(Colour.WHITE);
		index = new IntegerList();
		model = new MutableModel(Primitive.TRIANGLES, List.of(Point.LAYOUT, Colour.LAYOUT));
	}

	@Test
	void constructor() {
		// Check header
		assertEquals(Primitive.TRIANGLES, model.primitive());
		assertEquals(List.of(Point.LAYOUT, Colour.LAYOUT), model.layout());
		assertEquals(0, model.count());
		assertEquals(false, model.isIndexed());
		assertEquals(true, model.isEmpty());

		// Check vertices
		assertNotNull(model.vertices());
		assertEquals(0, model.vertices().count());

		// Check index
		assertNotNull(model.index());
		assertEquals(0, model.index().count());

		// Check buffers
		assertNotNull(model.vertexBuffer());
		assertNotNull(model.indexBuffer());
	}

	@Test
	void addVertex() {
		model.add(vertex);
		assertEquals(1, model.count());
		assertEquals(false, model.isIndexed());
		assertEquals(false, model.isEmpty());
		assertArrayEquals(new Vertex[]{vertex}, model.vertices().toArray());
	}

	@Test
	void addIndex() {
		model.add(vertex);
		model.add(0);
		assertEquals(1, model.count());
		assertEquals(true, model.isIndexed());
		assertArrayEquals(new int[]{0}, model.index().toArray());
	}

	@Test
	void addIndexInvalid() {
		assertThrows(IllegalArgumentException.class, () -> model.add(0));
	}

	private void triangle() {
		for(int n = 0; n < 3; ++n) {
			model.add(vertex);
		}
	}

	private void triangleIndex() {
		for(int n = 0; n < 3; ++n) {
			model.add(n);
		}
	}

	@Test
	void iterator() {
		// Add two triangles
		triangle();
		triangleIndex();
		triangleIndex();

		// Check iterator
		final Iterator<int[]> iterator = model.iterator();
		assertNotNull(iterator);
		assertArrayEquals(new int[]{0, 1, 2}, iterator.next());
		assertArrayEquals(new int[]{0, 1, 2}, iterator.next());
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void iteratorStripPrimitive() {
		// Create triangle strip model with two triangles
		model = new MutableModel(Primitive.TRIANGLE_STRIP, List.of(Point.LAYOUT, Colour.LAYOUT));
		model.add(vertex);
		triangle();
		triangleIndex();
		model.add(3);

		// Check iterator
		final Iterator<int[]> iterator = model.iterator();
		assertNotNull(iterator);
		assertArrayEquals(new int[]{0, 1, 2}, iterator.next());
		assertArrayEquals(new int[]{1, 2, 3}, iterator.next());
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void iteratorEmptyIndex() {
		final Iterator<int[]> iterator = model.iterator();
		assertNotNull(iterator);
		assertEquals(false, iterator.hasNext());
	}

//	@Test
//	void invalidVertexCount() {
//		assertThrows(IllegalArgumentException.class, () -> new MutableModel(Primitive.TRIANGLES, List.of(Point.LAYOUT), List.of(vertex), List.of()));
//		assertThrows(IllegalArgumentException.class, () -> new MutableModel(Primitive.TRIANGLES, List.of(Point.LAYOUT), List.of(vertex), List.of(0, 0)));
//	}
//
//	@Test
//	void invalidModelNormals() {
//		model = new MutableModel(Primitive.LINE_STRIP, List.of(Point.LAYOUT), List.of(vertex), List.of(0, 0));
//		assertThrows(IllegalArgumentException.class, () -> model.validate(true));
//	}

	@Test
	void vertexBuffer() {
		// Add a vertex
		model.add(vertex);

		// Check vertices
		final int count = (3 + 4) * Float.BYTES;
		final Bufferable vertices = model.vertexBuffer();
		assertNotNull(vertices);
		assertEquals(count, vertices.length());

		// Buffer model
		final ByteBuffer buffer = ByteBuffer.allocate(count);
		vertices.buffer(buffer);
		assertEquals(0, buffer.remaining());

		// Check vertex data
		final ByteBuffer expected = ByteBuffer.allocate(count);
		vertex.buffer(expected);
		assertEquals(expected.flip(), buffer.flip());
	}

	@Test
	void indexBuffer() {
		// Init index
		model.add(vertex);
		model.add(0);
		model.add(0);

		// Check index
		final Bufferable index = model.indexBuffer();
		assertNotNull(index);
		assertEquals(2 * Integer.BYTES, index.length());
	}
}
