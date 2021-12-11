package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.Point;

class DefaultModelTest {
	private DefaultModel model;
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = mock(Vertex.class);
		model = new DefaultModel(Primitive.TRIANGLE_STRIP);
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
		assertEquals(List.of(), model.layout());
		assertEquals(0, model.count());
		assertEquals(false, model.isIndexed());
		assertNotNull(model.vertices());
		assertNotNull(model.index());
	}

	@Test
	void layout() {
		model.layout(Point.LAYOUT);
		assertEquals(List.of(Point.LAYOUT), model.layout());
	}

	@Test
	void addVertex() {
		model.add(vertex);
		assertEquals(1, model.count());
		assertEquals(false, model.isIndexed());
	}

	@Test
	void addIndex() {
		model.add(vertex);
		model.add(0);
		model.add(0);
		assertEquals(2, model.count());
		assertEquals(true, model.isIndexed());
	}

	@Test
	void addIndexInvalid() {
		assertThrows(IllegalArgumentException.class, () -> model.add(0));
	}

	@Test
	void vertices() {
		// Add triangle
		model.layout(Point.LAYOUT);
		model.add(vertex);
		model.add(vertex);
		model.add(vertex);

		// Check vertices
		final Bufferable vertices = model.vertices();
		assertNotNull(vertices);
		assertEquals(3 * 3 * Float.BYTES, vertices.length());

		// Check vertex data
		final ByteBuffer bb = mock(ByteBuffer.class);
		vertices.buffer(bb);
		verify(vertex, times(3)).buffer(bb);

		// Check index
		assertEquals(false, model.isIndexed());
		assertEquals(0, model.index().length());
	}

	@Test
	void index() {
		// Build indexed triangle
		model.layout(Point.LAYOUT);
		model.add(vertex);
		model.add(0);
		model.add(0);
		model.add(0);

		// Check vertices
		final Bufferable vertices = model.vertices();
		assertNotNull(vertices);
		assertEquals(1 * 3 * Float.BYTES, vertices.length());

		// Check vertex data
		final ByteBuffer bb = mock(ByteBuffer.class);
		vertices.buffer(bb);
		verify(vertex, times(1)).buffer(bb);

		// Check index
		final Bufferable index = model.index();
		assertNotNull(index);
		assertEquals(3 * Integer.BYTES, vertices.length());

		// Check index data
		final IntBuffer buffer = mock(IntBuffer.class);
		when(bb.asIntBuffer()).thenReturn(buffer);
		index.buffer(bb);
		verify(buffer).put(new int[]{0, 0, 0});

		// Check direct index buffer
		when(buffer.isDirect()).thenReturn(true);
		index.buffer(bb);
		verify(buffer, times(3)).put(0);
	}
}
