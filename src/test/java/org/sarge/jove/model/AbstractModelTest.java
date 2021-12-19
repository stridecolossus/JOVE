package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.Point;

class AbstractModelTest {
	private AbstractModel model;

	@BeforeEach
	void before() {
		model = new AbstractModel(Primitive.TRIANGLE_STRIP, List.of(Point.LAYOUT)) {
			@Override
			public int count() {
				return 0;
			}

			@Override
			public Bufferable vertexBuffer() {
				return null;
			}

			@Override
			public boolean isIndexed() {
				return false;
			}

			@Override
			public Bufferable indexBuffer() {
				return null;
			}
		};
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
		assertEquals(List.of(Point.LAYOUT), model.layout());
	}

	@Test
	void validate() {
		model.validate();
	}

	@Test
	void equals() {
		assertEquals(model, model);
		assertNotEquals(model, null);
		assertNotEquals(model, mock(Model.class));
	}
}
