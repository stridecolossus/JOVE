package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.common.Vertex;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Model.Header;

class DefaultModelTest {
	private DefaultModel model;
	private Header header;
	private int[] indices;
	private Vertex vertex;

	@BeforeEach
	void before() {
		header = new Header(CompoundLayout.of(Point.LAYOUT), Primitive.TRIANGLES, 3);
		vertex = Vertex.of(Point.ORIGIN);
		indices = new int[]{1, 1, 1};
		model = new DefaultModel(header, List.of(vertex), indices);
	}

	@Test
	void constructor() {
		assertEquals(header, model.header());
	}

	@Test
	void unindexed() {
		model = new DefaultModel(header, List.of(vertex, vertex, vertex), null);
		assertEquals(Optional.empty(), model.index());
	}

	@Test
	void vertexBuffer() {
		final Bufferable vbo = model.vertices();
		assertNotNull(vbo);
		assertEquals(Point.LAYOUT.length(), vbo.length());

		final ByteBuffer bb = mock(ByteBuffer.class);
		when(bb.putFloat(anyFloat())).thenReturn(bb);
		vbo.buffer(bb);
		verify(bb, times(3)).putFloat(0);
	}

	@Test
	void indexBuffer() {
		final Bufferable index = model.index().orElseThrow();
		assertNotNull(index);
		assertEquals(3 * Integer.BYTES, index.length());

		final ByteBuffer bb = mock(ByteBuffer.class);
		final IntBuffer buffer = mock(IntBuffer.class);
		when(bb.asIntBuffer()).thenReturn(buffer);
		index.buffer(bb);
		verify(buffer).put(indices);
	}
}
