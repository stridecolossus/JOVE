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
		model = new DefaultModel(Primitive.TRIANGLE_STRIP, List.of(Point.LAYOUT), List.of(vertex), List.of(0, 0, 0));
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
		assertEquals(List.of(Point.LAYOUT), model.layout());
		assertEquals(3, model.count());
		assertEquals(true, model.isIndexed());
		assertNotNull(model.vertices());
		assertNotNull(model.index());
	}

	@Test
	void unindexed() {
		model = new DefaultModel(Primitive.TRIANGLE_STRIP, List.of(Point.LAYOUT), List.of(vertex, vertex, vertex), List.of());
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
		assertEquals(List.of(Point.LAYOUT), model.layout());
		assertEquals(3, model.count());
		assertEquals(false, model.isIndexed());
		assertNotNull(model.vertices());
		assertNotNull(model.index());
	}

	@Test
	void invalidVertexCount() {
		assertThrows(IllegalArgumentException.class, () -> new DefaultModel(Primitive.TRIANGLES, List.of(Point.LAYOUT), List.of(vertex), List.of()));
		assertThrows(IllegalArgumentException.class, () -> new DefaultModel(Primitive.TRIANGLES, List.of(Point.LAYOUT), List.of(vertex), List.of(0, 0)));
	}

	@Test
	void invalidModelNormals() {
		model = new DefaultModel(Primitive.LINE_STRIP, List.of(Point.LAYOUT), List.of(vertex), List.of(0, 0));
		assertThrows(IllegalArgumentException.class, () -> model.validate(true));
	}

	@Test
	void vertices() {
		// Check vertices
		final Bufferable vertices = model.vertices();
		assertNotNull(vertices);
		assertEquals(3 * Float.BYTES, vertices.length());

		// Check vertex data
		final ByteBuffer bb = mock(ByteBuffer.class);
		vertices.buffer(bb);
		verify(vertex).buffer(bb);
	}

	@Test
	void index() {
		final Bufferable index = model.index();
		assertNotNull(index);
		assertEquals(3 * Integer.BYTES, index.length());

		// Check index data
		final ByteBuffer bb = mock(ByteBuffer.class);
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
