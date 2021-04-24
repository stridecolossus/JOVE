package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Component;
import org.sarge.jove.model.Model.Header;
import org.sarge.jove.model.Vertex.Layout;

public class BufferedModelTest {
	private BufferedModel model;
	private Header header;
	private Bufferable vertices, index;

	@BeforeEach
	void before() {
		header = new Header(Primitive.TRIANGLES, new Layout(Component.POSITION), true);
		vertices = mock(Bufferable.class);
		index = mock(Bufferable.class);
		model = new BufferedModel(header, 3, vertices, index);
	}

	@Test
	void constructor() {
		assertEquals(header, model.header());
		assertEquals(3, model.count());
		assertEquals(true, model.isIndexed());
		assertEquals(vertices, model.vertexBuffer());
		assertEquals(Optional.of(index), model.indexBuffer());
	}

	@Test
	void unindexed() {
		model = new BufferedModel(header, 3, vertices, null);
		assertEquals(header, model.header());
		assertEquals(3, model.count());
		assertEquals(false, model.isIndexed());
		assertEquals(vertices, model.vertexBuffer());
		assertEquals(Optional.empty(), model.indexBuffer());
	}
}
