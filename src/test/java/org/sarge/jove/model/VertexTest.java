package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
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
		one = mock(Component.class);
		two = mock(Component.class);
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
		when(one.length()).thenReturn(1);
		when(two.length()).thenReturn(2);
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
		final Layout layout = Layout.floats(1);
		when(one.layout()).thenReturn(layout);

		final Vertex result = vertex.transform(List.of(layout));
		assertNotNull(result);
		assertEquals(List.of(one), result.components());
	}

	@Test
	void transformSelf() {
		when(one.layout()).thenReturn(Layout.floats(1));
		when(two.layout()).thenReturn(Layout.floats(2));
		final Vertex result = vertex.transform(List.of(one.layout(), two.layout()));
		assertEquals(vertex, result);
	}

	@Test
	void transformInvalidLayout() {
		final Layout layout = Layout.floats(1);
		assertThrows(IllegalArgumentException.class, () -> vertex.transform(List.of(layout)));
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
