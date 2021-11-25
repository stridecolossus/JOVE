package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.io.Bufferable;
import org.sarge.jove.model.Model.Header;

class DefaultModelTest {
	private DefaultModel model;
	private Header header;
	private int[] indices;
	private Vertex vertex;

	@BeforeEach
	void before() {
		header = new Header(List.of(Point.LAYOUT, Model.NORMALS), Primitive.TRIANGLES, 3);
		vertex = Vertex.of(Point.ORIGIN, new Vector(0, 0, 0));
		indices = new int[]{0, 0, 0};
		model = new DefaultModel(header, List.of(vertex, vertex), indices);
	}

	@Test
	void constructor() {
		assertEquals(header, model.header());
	}

	@Test
	void unindexed() {
		model = new DefaultModel(header, List.of(vertex), null);
		assertEquals(Optional.empty(), model.index());
	}

	@Test
	void vertexBuffer() {
		// Create VBO
		final Bufferable vbo = model.vertices();
		assertNotNull(vbo);
		assertEquals(2 * (3 + 3) * Float.BYTES, vbo.length());

		// Check VBO
		final ByteBuffer bb = mock(ByteBuffer.class);
		when(bb.putFloat(anyFloat())).thenReturn(bb);
		vbo.buffer(bb);
		verify(bb, times(2 * 2 * 3)).putFloat(0);
	}

	@Test
	void indexBuffer() {
		// Create index
		final Bufferable index = model.index().orElseThrow();
		assertNotNull(index);
		assertEquals(3 * Integer.BYTES, index.length());

		// Check index
		final ByteBuffer bb = mock(ByteBuffer.class);
		final IntBuffer buffer = mock(IntBuffer.class);
		when(bb.asIntBuffer()).thenReturn(buffer);
		index.buffer(bb);
		verify(buffer).put(indices);
	}

	@Test
	void transformSwapComponents() {
		final List<Layout> layout = List.of(Model.NORMALS, Point.LAYOUT);
		final DefaultModel result = model.transform(layout);
		assertNotNull(result);
		assertEquals(layout, result.header().layout());
	}

	@Test
	void transformRemoveComponent() {
		final List<Layout> layout = List.of(Point.LAYOUT);
		final DefaultModel result = model.transform(layout);
		assertNotNull(result);
		assertEquals(layout, result.header().layout());
	}

	@Test
	void transformInvalidLayout() {
		final List<Layout> layout = List.of(Layout.floats(4));
		assertThrows(IllegalArgumentException.class, () -> model.transform(layout));
	}
}
