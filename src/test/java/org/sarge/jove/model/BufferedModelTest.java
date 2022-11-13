package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.Point;

class BufferedModelTest {
	private BufferedModel model;
	private ByteSizedBufferable data;

	@BeforeEach
	void before() {
		data = mock(ByteSizedBufferable.class);
		model = new BufferedModel(Primitive.TRIANGLES, 3, new Layout(Point.LAYOUT), data, data);
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLES, model.primitive());
		assertEquals(3, model.count());
		assertEquals(new Layout(Point.LAYOUT), model.layout());
		assertEquals(true, model.isIndexed());
		assertEquals(data, model.vertices());
		assertEquals(Optional.of(data), model.index());
	}

	@Test
	void unindexed() {
		model = new BufferedModel(Primitive.TRIANGLES, 3, new Layout(Point.LAYOUT), data, null);
		assertEquals(3, model.count());
		assertEquals(false, model.isIndexed());
		assertEquals(Optional.empty(), model.index());
	}
}
