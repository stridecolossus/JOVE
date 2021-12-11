package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;
import org.sarge.jove.model.Model.AbstractModel;

class ModelTest {
	private Model model;

	@BeforeEach
	void before() {
		model = new AbstractModel(Primitive.TRIANGLE_STRIP) {
			@Override
			public List<Layout> layout() {
				return List.of();
			}

			@Override
			public int count() {
				return 0;
			}

			@Override
			public Bufferable vertices() {
				return null;
			}

			@Override
			public boolean isIndexed() {
				return false;
			}

			@Override
			public Bufferable index() {
				return null;
			}
		};
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
	}

	@Test
	void equals() {
		assertEquals(model, model);
		assertNotEquals(model, null);
		assertNotEquals(model, mock(Model.class));
	}
}
