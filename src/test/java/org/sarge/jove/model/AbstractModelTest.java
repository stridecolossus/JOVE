package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Normal;

public class AbstractModelTest {
	private static class MockAbstractModel extends AbstractModel {
		public MockAbstractModel(Primitive primitive) {
			super(primitive, new Layout(Normal.LAYOUT));
		}

		@Override
		public int count() {
			return 0;
		}

		@Override
		public boolean isIndexed() {
			return false;
		}
	}

	private AbstractModel model;

	@BeforeEach
	void before() {
		model = new MockAbstractModel(Primitive.TRIANGLES);
	}

	@Test
	void constructor() {
		assertEquals(Primitive.TRIANGLES, model.primitive());
		assertEquals(new Layout(Normal.LAYOUT), model.layout());
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> new MockAbstractModel(Primitive.LINES));
	}

	@Test
	void equals() {
		assertEquals(model, model);
		assertEquals(model, new MockAbstractModel(Primitive.TRIANGLES));
		assertNotEquals(model, null);
		assertNotEquals(model, new MockAbstractModel(Primitive.TRIANGLE_STRIP));
	}
}
