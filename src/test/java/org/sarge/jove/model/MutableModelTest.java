package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.util.BufferHelper;

public class MutableModelTest {
	private MutableModel model;
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex();
		model = new MutableModel();
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
		assertEquals(List.of(), model.layout());
		assertEquals(0, model.count());
		assertNotNull(model.vertices());
		assertNotNull(model.index());
		assertEquals(0, model.vertices().count());
		assertEquals(0, model.index().count());
	}

	@Test
	void primitive() {
		model.primitive(Primitive.LINES);
		assertEquals(Primitive.LINES, model.primitive());
	}

	@Test
	void layout() {
		model.layout(Point.LAYOUT);
		model.layout(Colour.LAYOUT);
		assertEquals(List.of(Point.LAYOUT, Colour.LAYOUT), model.layout());
	}

	@Test
	void add() {
		model.add(vertex);
		model.add(vertex);
		assertEquals(2, model.count());
		assertArrayEquals(new Vertex[]{vertex, vertex}, model.vertices().toArray());
	}

	@Test
	void index() {
		model.add(vertex);
		model.add(0);
		model.add(0);
		model.add(0);
		assertEquals(3, model.count());
		assertArrayEquals(new int[]{0, 0, 0}, model.index().toArray());
	}

	@Test
	void indexInvalid() {
		assertThrows(IllegalArgumentException.class, () -> model.add(0));
	}

	@Test
	void restart() {
		model.restart();
		assertArrayEquals(new int[]{-1}, model.index().toArray());
	}

	@Test
	void vertexBuffer() {
		// Add triangle
		model.layout(Point.LAYOUT);
		model.add(vertex);
		model.add(vertex);
		model.add(vertex);

		// Build vertex buffer
		final Bufferable vertices = model.vertexBuffer();
		assertNotNull(vertices);

		// Check length
		final int len = 3 * 3 * Float.BYTES;
		assertEquals(len, vertices.length());

		// Build expected buffer
		final ByteBuffer expected = BufferHelper.allocate(len);
		vertex.buffer(expected);
		vertex.buffer(expected);
		vertex.buffer(expected);

		// Check buffer
		final ByteBuffer actual = BufferHelper.allocate(len);
		vertices.buffer(actual);
		assertEquals(expected, actual);
	}

	@Test
	void indexBuffer() {
		// Add triangle
		model.add(vertex);
		model.add(0);
		model.add(0);
		model.add(0);

		// Build index buffer
		final Bufferable index = model.indexBuffer().get();
		assertNotNull(index);

		// Check length
		final int len = 3 * Short.BYTES;
		assertEquals(len, index.length());

		// Build expected index
		final ByteBuffer expected = BufferHelper.allocate(len);
		expected.putShort((short) 0);
		expected.putShort((short) 0);
		expected.putShort((short) 0);

		// Check index
		final ByteBuffer actual = BufferHelper.allocate(len);
		index.buffer(actual);
		assertEquals(expected, actual);
	}

	@Test
	void largeIndex() {
		// Create a model with an integer index
		model.add(vertex);
		for(int n = 0; n < Model.SHORT; ++n) {
			model.add(0);
		}

		// Check buffer
		final Bufferable index = model.indexBuffer().get();
		assertNotNull(index);
		assertEquals(2 * Model.SHORT, index.length());
	}
}
