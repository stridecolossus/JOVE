package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.io.Bufferable;

public class VertexTest {
	private Vertex vertex;
	private Bufferable one, two;

	@BeforeEach
	void before() {
		// Create a component
		one = mock(Bufferable.class);
		when(one.length()).thenReturn(1);

		// And another
		two = mock(Bufferable.class);
		when(two.length()).thenReturn(2);

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
		assertEquals(1 + 2, vertex.length());
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
