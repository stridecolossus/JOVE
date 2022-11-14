package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.*;

class SimpleVertexTest {
	private SimpleVertex vertex;

	@BeforeEach
	void before() {
		vertex = new SimpleVertex(Point.ORIGIN);
	}

	@Test
	void constructor() {
		assertEquals(Point.ORIGIN, vertex.position());
	}

	@Test
	void normal() {
		assertThrows(UnsupportedOperationException.class, () -> vertex.normal(Axis.Y));
	}

	@Test
	void layout() {
		assertEquals(new Layout(Point.LAYOUT), vertex.layout());
	}

	@Test
	void buffer() {
		final var bb = mock(ByteBuffer.class);
		vertex.buffer(bb);
		verify(bb, times(3)).putFloat(0f);
	}

	@Test
	void equals() {
		assertEquals(vertex, vertex);
		assertEquals(vertex, new SimpleVertex(Point.ORIGIN));
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, mock(Vertex.class));
	}
}
