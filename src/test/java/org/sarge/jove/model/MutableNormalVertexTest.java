package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

class MutableNormalVertexTest {
	private MutableNormalVertex vertex;

	@BeforeEach
	void before() {
		vertex = new MutableNormalVertex(Point.ORIGIN);
	}

	@Test
	void position() {
		assertEquals(Point.ORIGIN, vertex.position());
	}

	@Test
	void normal() {
		vertex.add(Axis.Y);
		assertEquals(Axis.Y, vertex.normal());
	}

	@Test
	void accumulated() {
		vertex.add(Axis.X);
		vertex.add(Axis.X.invert());
		vertex.add(Axis.Y);
		assertEquals(Axis.Y, vertex.normal());
	}

	@Test
	void buffer() {
		final var buffer = mock(ByteBuffer.class);
		vertex.add(Axis.Y);
		vertex.buffer(buffer);
		verify(buffer, times(6)).putFloat(anyFloat());
	}

	@Test
	void equals() {
		assertEquals(vertex, vertex);
		assertNotEquals(vertex, null);
		assertNotEquals(vertex, new MutableNormalVertex(new Point(1, 2, 3)));
	}
}
