package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Vertex.Component;

class DefaultModelTest {
	private DefaultModel model;
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex(Point.ORIGIN, null, null, Colour.WHITE);
		model = new DefaultModel(Primitive.TRIANGLE_STRIP, List.of(Point.LAYOUT, Colour.LAYOUT), List.of(vertex), List.of(0, 0, 0));
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
		assertEquals(List.of(Point.LAYOUT, Colour.LAYOUT), model.layout());
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
	void transform() {
		model = model.transform(Component.POSITION);
		assertNotNull(model);
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
		assertEquals(List.of(Point.LAYOUT), model.layout());
		assertEquals(3, model.count());
		assertEquals(true, model.isIndexed());
		assertNotNull(model.vertices());
		assertNotNull(model.index());
	}

	@Test
	void transformSelf() {
		assertSame(model, model.transform(Component.POSITION, Component.COLOUR));
	}

	@Test
	void transformInvalidComponent() {
		assertThrows(IllegalArgumentException.class, () -> model.transform(Component.POSITION, Component.NORMAL));
	}

	@Test
	void vertices() {
		// Check vertices
		final int count = (3 + 4) * Float.BYTES;
		final Bufferable vertices = model.vertices();
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
