package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.model.Model.Header;

public class BufferedModelTest {
	private BufferedModel model;
	private Header header;
	private Bufferable vertices, index;

	@BeforeEach
	void before() {
		header = new Header(Primitive.TRIANGLES, true, 3);
		vertices = mock(Bufferable.class);
		index = mock(Bufferable.class);
		model = new BufferedModel(header, vertices, index);
	}

	@Test
	void constructor() {
		assertEquals(header, model.header());
		assertEquals(true, model.isIndexed());
		assertEquals(vertices, model.vertexBuffer());
		assertEquals(Optional.of(index), model.indexBuffer());
	}

	@Test
	void unindexed() {
		model = new BufferedModel(header, vertices, null);
		assertEquals(header, model.header());
		assertEquals(false, model.isIndexed());
		assertEquals(vertices, model.vertexBuffer());
		assertEquals(Optional.empty(), model.indexBuffer());
	}
}
