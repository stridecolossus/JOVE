package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

class DefaultVertexTest {
	private DefaultVertex vertex;

	@BeforeEach
	void before() {
		vertex = new DefaultVertex(Point.ORIGIN, Coordinate2D.TOP_LEFT);
	}

	@Test
	void position() {
		assertEquals(Point.ORIGIN, vertex.position());
	}

	@Test
	void layout() {
		assertEquals(new Layout(Point.LAYOUT, Coordinate2D.LAYOUT), vertex.layout());
	}

	@Test
	void normal() {
		final DefaultVertex result = vertex.add(Axis.Y);
		assertEquals(Axis.Y, result.normal());
		assertEquals(new Layout(Point.LAYOUT, Normal.LAYOUT, Coordinate2D.LAYOUT), result.layout());
	}

	@Test
	void already() {
		assertThrows(IllegalStateException.class, () -> vertex.add(Axis.Y).add(Axis.Y));
	}

	@Test
	void buffer() {
		final ByteBuffer bb = mock(ByteBuffer.class);
		when(bb.putFloat(0f)).thenReturn(bb);
		vertex.buffer(bb);
		verify(bb, times(3 + 2)).putFloat(0f);
	}

	@Test
	void equals() {
		assertEquals(vertex, vertex);
		assertEquals(vertex, new DefaultVertex(Point.ORIGIN, Coordinate2D.TOP_LEFT));
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, mock(Vertex.class));
	}
}
