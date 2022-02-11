package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;

public class BufferedModelTest {
	private BufferedModel model;
	private Bufferable vertices, index;

	@BeforeEach
	void before() {
		vertices = mock(Bufferable.class);
		index = mock(Bufferable.class);
		model = new BufferedModel(Primitive.TRIANGLES, List.of(Layout.floats(3)), 3, vertices, index);
	}

	@Test
	void constructor() {
		when(index.length()).thenReturn(3);
		assertEquals(List.of(Layout.floats(3)), model.layout());
		assertEquals(Primitive.TRIANGLES, model.primitive());
		assertEquals(3, model.count());
		assertEquals(vertices, model.vertexBuffer());
		assertEquals(Optional.of(index), model.indexBuffer());
	}

	@Test
	void unindexed() {
		when(index.length()).thenReturn(0);
		assertEquals(3, model.count());
		assertEquals(Optional.empty(), model.indexBuffer());
	}
}
