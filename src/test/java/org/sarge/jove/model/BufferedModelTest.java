package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.ByteData.Source;
import org.sarge.jove.common.Component.Layout;
import org.sarge.jove.model.Model.Header;

public class BufferedModelTest {
	private BufferedModel model;
	private Header header;
	private Source vertices, index;

	@BeforeEach
	void before() {
		header = new Header(List.of(Layout.of(2)), Primitive.TRIANGLES, 3, true);
		vertices = mock(Source.class);
		index = mock(Source.class);
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
