package org.sarge.jove.model;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.util.BufferHelper;

public class DefaultModelTest {
	private DefaultModel model;
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex();
		model = new DefaultModel(Primitive.LINE_STRIP, List.of(Point.LAYOUT), List.of(vertex, vertex), List.of(0, 0, 0));
	}

	@Test
	void constructor() {
		assertEquals(Primitive.LINE_STRIP, model.primitive());
		assertEquals(List.of(Point.LAYOUT), model.layout());
		assertEquals(3, model.count());
		assertNotNull(model.vertices());
		assertNotNull(model.index());
		assertArrayEquals(new Vertex[]{vertex, vertex}, model.vertices().toArray());
		assertArrayEquals(new int[]{0, 0, 0}, model.index().toArray());
	}

	@Test
	void vertices() {
		// Build vertex buffer
		final Bufferable vertices = model.vertexBuffer();
		assertNotNull(vertices);

		// Check length
		final int len = 2 * 3 * Float.BYTES;
		assertEquals(len, vertices.length());

		// Build expected buffer
		final ByteBuffer expected = BufferHelper.allocate(len);
		vertex.buffer(expected);
		vertex.buffer(expected);

		// Check buffer
		final ByteBuffer actual = BufferHelper.allocate(len);
		vertices.buffer(actual);
		assertEquals(expected, actual);
	}

	@Test
	void index() {
		// Build index buffer
		final Optional<Bufferable> index = model.indexBuffer();
		assertNotNull(index);
		assertTrue(index.isPresent());

		// Check length
		final int len = 3 * Short.BYTES;
		assertEquals(len, index.get().length());

		// Build expected index
		final ByteBuffer expected = BufferHelper.allocate(len);
		expected.putShort((short) 0);
		expected.putShort((short) 0);
		expected.putShort((short) 0);

		// Check index
		final ByteBuffer actual = BufferHelper.allocate(len);
		index.get().buffer(actual);
		assertEquals(expected, actual);
	}

	@Test
	void largeIndex() {
		// Create model with an integer index
		final List<Vertex> vertices = IntStream.range(0, (int) Model.SHORT).mapToObj(n -> vertex).collect(toList());
		model = new DefaultModel(Primitive.POINTS, List.of(Point.LAYOUT), vertices, List.of(0));

		// Check index buffer
		final Optional<Bufferable> index = model.indexBuffer();
		assertNotNull(index);
		assertTrue(index.isPresent());
		assertEquals(Integer.BYTES, index.get().length());
	}
}
