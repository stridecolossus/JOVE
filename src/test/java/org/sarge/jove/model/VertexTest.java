package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Component;
import org.sarge.jove.common.Layout;

public class VertexTest {
	private Vertex vertex;
	private Component one, two;

	@BeforeEach
	void before() {
		// Create a component
		one = spy(Component.class);
		when(one.layout()).thenReturn(Layout.of(2));

		// Create another component
		two = spy(Component.class);
		when(two.layout()).thenReturn(Layout.of(3));

		// Create vertex
		vertex = new Vertex(List.of(one, two));
	}

	@Test
	void constructor() {
		assertEquals(List.of(one, two), vertex.components());
	}

	@Test
	void of() {
		assertEquals(vertex, Vertex.of(one, two));
	}

	@Test
	void length() {
		assertEquals((2 + 3) * Float.BYTES, vertex.length());
	}

	@Test
	void buffer() {
		final ByteBuffer bb = mock(ByteBuffer.class);
		vertex.buffer(bb);
		verify(one).buffer(bb);
		verify(two).buffer(bb);
	}

	@Test
	void transform() {
		final int[] map = {1, 0};
		final Vertex transformed = vertex.transform(map);
		assertEquals(Vertex.of(two, one), transformed);
	}

	@Test
	void transformInvalidIndex() {
		assertThrows(IndexOutOfBoundsException.class, () -> vertex.transform(new int[]{2}));
	}

	@Test
	void hash() {
		assertEquals(Objects.hash(List.of(one, two)), vertex.hashCode());
	}

	@Test
	void equals() {
		assertEquals(true, vertex.equals(vertex));
		assertEquals(true, vertex.equals(Vertex.of(one, two)));
		assertEquals(false, vertex.equals(null));
		assertEquals(false, vertex.equals(Vertex.of(one)));
	}
}
